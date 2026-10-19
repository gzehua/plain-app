package com.ismartcoding.plain.ui.web

import com.ismartcoding.plain.preferences.*

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.IgnoreBatteryOptimizationResultEvent
import com.ismartcoding.plain.app.events.PermissionsResultEvent
import com.ismartcoding.plain.app.events.RequestPermissionsEvent
import com.ismartcoding.plain.app.events.HostEvent
import com.ismartcoding.plain.app.events.WindowFocusChangedEvent
import com.ismartcoding.plain.lib.JsonHelper.jsonEncode
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.ui.permissions.PermissionItem
import com.ismartcoding.plain.features.permissions.getGrantedWebPermissionsAsync
import com.ismartcoding.plain.features.permissions.getWebList
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.platform.permissions.isGranted
import com.ismartcoding.plain.platform.system.isIgnoringBatteryOptimizations
import com.ismartcoding.plain.platform.notifications.toggleNotificationListener
import com.ismartcoding.plain.ui.components.DialogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun WebSettingsEffects(
    permissionList: MutableState<List<PermissionItem>>,
    shouldIgnoreOptimize: MutableState<Boolean>,
    systemAlertWindow: MutableState<Boolean>,
    notificationListenerGranted: MutableState<Boolean>,
) {
    val sharedFlow = Channel.sharedFlow

    LaunchedEffect(sharedFlow) {
        sharedFlow.collect { event ->
            when (event) {
                is PermissionsResultEvent -> {
                    permissionList.value = getWebList()
                    systemAlertWindow.value = Permission.SYSTEM_ALERT_WINDOW.isGranted()
                    notificationListenerGranted.value = Permission.NOTIFICATION_LISTENER.isGranted()
                    if (event.map[Permission.NOTIFICATION_LISTENER.toSysPermission()] == true) {
                        toggleNotificationListener(UserPrefs.desktopAccess.value)
                    }
                }
                is WindowFocusChangedEvent -> {
                    shouldIgnoreOptimize.value = !isIgnoringBatteryOptimizations()
                    notificationListenerGranted.value = Permission.NOTIFICATION_LISTENER.isGranted()
                }
                is IgnoreBatteryOptimizationResultEvent -> {
                    if (shouldIgnoreOptimize.value) {
                        coIO {
                            DialogHelper.showLoading(); delay(1000); DialogHelper.hideLoading()
                            shouldIgnoreOptimize.value = !isIgnoringBatteryOptimizations()
                        }
                    }
                }
            }
        }
    }
}

internal fun togglePermission(scope: CoroutineScope, m: PermissionItem, enable: Boolean) {
    scope.launch {
        RustSystemState.setApiPermission(m.permission, enable)
        // The web `permissions` list is backed by this preference and toggling
        // a switch never produces a PermissionsResultEvent — push the fresh
        // snapshot so web clients refetch.
        sendEvent(HostEvent(EventType.PERMISSIONS_UPDATED, jsonEncode(getGrantedWebPermissionsAsync())))
        if (m.permission == Permission.NOTIFICATION_LISTENER) {
            val webEnabled = UserPrefs.desktopAccess.value
            toggleNotificationListener(enable && webEnabled)
        }
        if (enable) {
            val ps = m.permissions.filter { !it.isGranted() }
            if (ps.isNotEmpty()) sendEvent(RequestPermissionsEvent(*ps.toTypedArray()))
        }
    }
}

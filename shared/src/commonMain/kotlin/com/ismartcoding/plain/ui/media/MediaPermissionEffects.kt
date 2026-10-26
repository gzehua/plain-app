package com.ismartcoding.plain.ui.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import com.ismartcoding.plain.events.PermissionsResultEvent
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.platform.system.AppFeatureType
import com.ismartcoding.plain.platform.system.hasPermission
import com.ismartcoding.plain.ui.components.StoragePermissionResumeEffect
import com.ismartcoding.plain.ui.components.refreshStoragePermission
import com.ismartcoding.plain.ui.navigation.NavLoadGate

@Composable
internal fun MediaPermissionEffects(
    hasPermission: MutableState<Boolean>,
    initialLoadGate: NavLoadGate?,
    reload: () -> Unit,
) {
    LaunchedEffect(Unit) {
        hasPermission.value = AppFeatureType.FILES.hasPermission()
        if (hasPermission.value) {
            initialLoadGate?.await()
            reload()
        }
    }
    LaunchedEffect(Channel.sharedFlow) {
        Channel.sharedFlow.collect { event ->
            if (event is PermissionsResultEvent) refreshStoragePermission(hasPermission, reload)
        }
    }
    StoragePermissionResumeEffect(hasPermission, reload)
}

package com.ismartcoding.plain.ui.settings

import com.ismartcoding.plain.ui.state.launchSafe

import com.ismartcoding.plain.preferences.*
import com.ismartcoding.plain.ui.i18n.LocaleHelper

import com.ismartcoding.plain.i18n.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismartcoding.plain.app.events.IgnoreBatteryOptimizationEvent
import com.ismartcoding.plain.app.events.KeepAwakeChangedEvent
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.app.checkHttpServerAsync
import com.ismartcoding.plain.platform.system.isIgnoringBatteryOptimizations
import com.ismartcoding.plain.platform.system.relaunchApp
import com.ismartcoding.plain.ui.components.DialogHelper
import kotlinx.coroutines.launch
import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.rust.RustEventHandler
import com.ismartcoding.plain.rust.native.RustNativeApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onSubscription

class DesktopAccessSettingsViewModel : ViewModel() {
    val lanIpv4Addresses = MutableStateFlow<List<String>?>(null)

    suspend fun observeNetworkAddresses() {
        RustEventHandler.events.onSubscription { refreshNetworkAddresses() }.collect { event ->
            if (event.type == EventType.NETWORK_CHANGED) refreshNetworkAddresses()
        }
    }

    private suspend fun refreshNetworkAddresses() {
        try {
            lanIpv4Addresses.value = RustNativeApi.lanIpv4Addresses()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            LogCat.e("Load network addresses", error)
        }
    }

    fun dig() {
        viewModelScope.launch {
            DialogHelper.showLoading()
            val errorMessage = LocaleHelper.getStringAsync(Res.string.http_server_error)
            val serverUp = checkHttpServerAsync()
            DialogHelper.hideLoading()
            if (!serverUp) {
                DialogHelper.showConfirmDialog(
                    LocaleHelper.getStringAsync(Res.string.error),
                    errorMessage,
                    confirmButton = Pair(LocaleHelper.getStringAsync(Res.string.ok)) {},
                    dismissButton = Pair(LocaleHelper.getStringAsync(Res.string.relaunch_app)) { relaunchApp() },
                )
            } else {
                DialogHelper.showConfirmDialog(LocaleHelper.getStringAsync(Res.string.confirm), LocaleHelper.getStringAsync(Res.string.http_server_ok))
            }
        }
    }

    fun requestIgnoreBatteryOptimization() {
        if (!isIgnoringBatteryOptimizations()) {
            sendEvent(IgnoreBatteryOptimizationEvent())
        }
    }

    fun enableKeepAwake(enable: Boolean) {
        viewModelScope.launchSafe {
            Prefs.set(PrefKeys.KEEP_AWAKE, enable)
            sendEvent(KeepAwakeChangedEvent(enable))
        }
    }
}

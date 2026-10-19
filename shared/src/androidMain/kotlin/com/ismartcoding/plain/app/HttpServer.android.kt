package com.ismartcoding.plain.app

import com.ismartcoding.plain.platform.system.HttpServerResources
import com.ismartcoding.plain.ui.i18n.LocaleHelper

import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.permissions.isEnabledAsync

import com.ismartcoding.plain.preferences.*

import android.content.Intent
import androidx.core.content.ContextCompat
import com.ismartcoding.plain.i18n.background_online_failed
import com.ismartcoding.plain.app.Constants
import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.platform.network.aware.WifiAwareTransport
import com.ismartcoding.plain.features.settings.HttpServerState
import com.ismartcoding.plain.features.telephony.sms.SmsProviderObserver
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.services.HttpServerService
import com.ismartcoding.plain.services.PNotificationListenerService
import com.ismartcoding.plain.app.HttpServerManager
import com.ismartcoding.plain.features.nearby.RustMdnsRuntime
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

// The SMS/MMS hooks below serve the web desktop bridge, whose only clients are
// connected web sessions — so they live exactly as long as the server:
// - SmsProviderObserver pushes a debounced WS event when the phone's SMS
//   database changes, so connected desktops refresh;
actual suspend fun onHttpServerStarted() {
    val service = appContext
    HttpServerResources.start()
    PNotificationListenerService.toggle(service, Permission.NOTIFICATION_LISTENER.isEnabledAsync())
    SmsProviderObserver.start(service)
}

actual suspend fun onHttpServerStopped() {
    HttpServerResources.stop()
    SmsProviderObserver.stop()
    PNotificationListenerService.toggle(appContext, false)
}

/** Starts optional background retention independently of the HTTP server. */
actual fun startHttpServerService() {
    if (!com.ismartcoding.plain.preferences.UserPrefs.service.value) return
    HttpServerManager.backgroundState.value = HttpServerState.STARTING
    HttpServerManager.backgroundError.value = ""
    coIO {
        var retry = 3
        val context = appContext
        while (retry > 0) {
            try {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, HttpServerService::class.java),
                )
                return@coIO
            } catch (ex: Exception) {
                LogCat.e(ex.toString())
                delay(500.milliseconds)
                retry--
            }
        }
        HttpServerManager.backgroundError.value = LocaleHelper.getString(com.ismartcoding.plain.i18n.Res.string.background_online_failed)
        HttpServerManager.backgroundState.value = HttpServerState.ERROR
    }
}

/** Stops background retention while the app-owned HTTP server keeps running. */
actual suspend fun stopHttpServiceAsync(): Unit = withIO {
    withContext(NonCancellable) {
        if (HttpServerService.isRunning()) HttpServerManager.backgroundState.value = HttpServerState.STOPPING
        appContext.stopService(Intent(appContext, HttpServerService::class.java))
        if (!HttpServerService.isRunning()) HttpServerManager.backgroundState.value = HttpServerState.OFF
        HttpServerManager.backgroundError.value = ""
    }
}

actual fun getAwareAttachStatus(): String =
    if (WifiAwareTransport.awareSession != null) "attached" else "not attached"

actual fun getAwareDiscoveredPeerCount(): Int = WifiAwareTransport.discoveredPeerCount

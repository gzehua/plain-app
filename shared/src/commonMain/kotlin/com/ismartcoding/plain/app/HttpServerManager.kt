package com.ismartcoding.plain.app

import com.ismartcoding.plain.app.startHttpServerAsync

import com.ismartcoding.plain.preferences.*

import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.features.settings.HttpServerState
import com.ismartcoding.plain.app.events.StartHttpServerEvent
import com.ismartcoding.plain.util.UrlHelper
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.notifications.generateNotificationId
import com.ismartcoding.plain.platform.system.Capability
import com.ismartcoding.plain.platform.system.getDeviceCapabilities
import com.ismartcoding.plain.platform.permissions.isGranted
import com.ismartcoding.plain.app.stopHttpServiceAsync
import kotlinx.coroutines.flow.MutableStateFlow

/** Platform service intent, UI lifecycle state . */
object HttpServerManager {
    val serverState = MutableStateFlow(HttpServerState.OFF)
    val backgroundState = MutableStateFlow(HttpServerState.OFF)
    val backgroundError = MutableStateFlow("")

    /** Last server start error message, empty when the server is healthy. */
    val httpServerError = MutableStateFlow("")

    /** Stable notification id used for the foreground service and server-status notifications. */
    val notificationId: Int by lazy { generateNotificationId() }

    // ----------------------------------------------------------------------------------
    // Service lifecycle intent. All serverState writes stay inside the start
    // orchestrator and the NonCancellable stop body (single-owner state);
    // these entry points only dispatch commands and gate on permissions.
    // ----------------------------------------------------------------------------------

    /** Dispatch the start command (idempotent — the service dedupes). */
    fun dispatchStart() {
        LogCat.d("dispatchStartHttpServer")
        coIO { sendEvent(StartHttpServerEvent()) }
    }

    /**
     * Start gated on the notification permission: dispatch directly when a
     * foreground-service start is allowed; a UI-initiated start requests the
     * system permission in place, auto paths stay silent (a
     * foreground service without its notification is invisible and easily
     * killed).
     */
    fun requestStart(fromUi: Boolean) {
        if (Capability.BACKGROUND_SERVICE !in getDeviceCapabilities()) return
        if (!UserPrefs.service.value || backgroundState.value == HttpServerState.ON || backgroundState.value.isProcessing()) return
        if (Permission.POST_NOTIFICATIONS.isGranted()) {
            dispatchStart()
            return
        }
        if (fromUi) {
            sendEvent(com.ismartcoding.plain.app.events.RequestPermissionsEvent(Permission.POST_NOTIFICATIONS))
        }
    }

    /** The service preference controls background retention, not HTTP availability. */
    fun setBackgroundEnabled(enable: Boolean, fromUi: Boolean = true) = coIO {
        check(!enable || Capability.BACKGROUND_SERVICE in getDeviceCapabilities()) { "Unsupported capability BACKGROUND_SERVICE" }
        UserPrefs.service.value = enable
        if (enable) requestStart(fromUi = fromUi) else stopHttpServiceAsync()
    }

    fun ensureStarted() {
        coIO {
            runCatching {
                startHttpServerAsync()
                if (UserPrefs.service.value && backgroundState.value == HttpServerState.OFF) {
                    requestStart(fromUi = false)
                }
            }.onFailure { LogCat.e("ensureStarted failed: ${it.message}") }
        }
    }

    /**
     * Body of the foreground-service notification: the http and https URLs the
     * client should open.
     */
    fun getNotificationContent(): String {
        val ip = TempData.mdnsHostname
        val http = UrlHelper.buildUrl("http", ip, UserPrefs.httpPort.value)
        val https = UrlHelper.buildUrl("https", ip, UserPrefs.httpsPort.value)
        return "$http\n$https"
    }

}

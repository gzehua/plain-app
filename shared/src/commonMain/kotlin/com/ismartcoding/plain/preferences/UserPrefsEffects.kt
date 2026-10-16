package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.features.dlna.DlnaRendererState
import com.ismartcoding.plain.app.HttpServerManager
import com.ismartcoding.plain.features.feeds.feedWorkerCancelRepeat
import com.ismartcoding.plain.features.feeds.feedWorkerStartRepeat
import com.ismartcoding.plain.platform.media.onScreenMirrorQualityChanged
import com.ismartcoding.plain.platform.system.setSystemLocale
import com.ismartcoding.plain.app.stopHttpServiceAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

/**
 * The OS-level consequences of a settings change.
 *
 * Rust owns the stored value and its derived events; these are the effects that
 * only this process can perform. They run off a before/after diff of the store
 * so a local write and a change pushed by a remote client are handled the same
 * way — the source of the write never reaches this file.
 */
internal object UserPrefsEffects {
    /** Stopping the service is suspend; the rest of the effects are not. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun applyChanges(previous: JsonObject, current: JsonObject) {
        if (changed(previous, current, DEVICE_NAME)) {
            TempData.deviceName.value = UserPrefs.deviceName.value
        }
        if (changed(previous, current, SCREEN_MIRROR_QUALITY)) {
            onScreenMirrorQualityChanged(UserPrefs.screenMirrorQuality.value.mode)
        }
        if (changed(previous, current, DLNA)) {
            if (UserPrefs.dlna.value) DlnaRendererState.start() else DlnaRendererState.stop()
        }
        if (changed(previous, current, FEED_AUTO_REFRESH, FEED_AUTO_REFRESH_INTERVAL, FEED_AUTO_REFRESH_ONLY_WIFI)) {
            if (UserPrefs.feedAutoRefresh.value) feedWorkerStartRepeat() else feedWorkerCancelRepeat()
        }
        if (changed(previous, current, LOCALE)) {
            setSystemLocale(UserPrefs.localeValue())
        }
        if (changed(previous, current, SERVICE)) {
            if (UserPrefs.service.value) HttpServerManager.requestStart(fromUi = false)
            else scope.launch { stopHttpServiceAsync() }
        }
    }

    private fun changed(previous: JsonObject, current: JsonObject, vararg keys: String): Boolean =
        keys.any { previous[it] != current[it] }

    // Storage keys are the Rust serde names — the same strings the GraphQL
    // schema exposes, so a remote client and this diff agree on what changed.
    private const val DEVICE_NAME = "deviceName"
    private const val SCREEN_MIRROR_QUALITY = "screenMirrorQuality"
    private const val DLNA = "dlna"
    private const val FEED_AUTO_REFRESH = "feedAutoRefresh"
    private const val FEED_AUTO_REFRESH_INTERVAL = "feedAutoRefreshIntervalSec"
    private const val FEED_AUTO_REFRESH_ONLY_WIFI = "feedAutoRefreshOnlyWifi"
    private const val LOCALE = "locale"
    private const val SERVICE = "service"
}
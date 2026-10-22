package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.app.HttpServerManager
import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.app.stopHttpServiceAsync
import com.ismartcoding.plain.platform.media.onScreenMirrorQualityChanged
import com.ismartcoding.plain.platform.system.setSystemLocale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

internal object PrefsEffects {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun applyChanges(previous: JsonObject, current: JsonObject) {
        fun changed(key: String) = previous[key] != current[key]
        if (changed("deviceName")) {
            TempData.deviceName.value = Prefs.deviceName.value
        }
        if (changed("screenMirrorQuality")) {
            onScreenMirrorQualityChanged(Prefs.screenMirrorQuality.value.mode)
        }
        if (changed("locale")) {
            setSystemLocale(Prefs.localeValue())
        }
        if (changed("backgroundServiceEnabled")) {
            if (Prefs.backgroundServiceEnabled.value) HttpServerManager.requestStart(fromUi = false)
            else scope.launch { stopHttpServiceAsync() }
        }
    }

}

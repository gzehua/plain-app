package com.ismartcoding.plain.app

import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.features.chat.ChatCacher
import com.ismartcoding.plain.features.chat.channel.ChannelCacher
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.preferences.*
import com.ismartcoding.plain.rust.host.audio.AudioEngineHost
import com.ismartcoding.plain.app.NetworkState

/**
 * Shared preference and TempData initialization, called by both Android
 * (`MainAppHelper`) and iOS (`MainViewController`) during app startup.
 *
 * Platform-specific initialization (Android: media duration cache, etc.)
 * stays in the platform modules and is called before
 * or after this function as needed.
 */
suspend fun initCommonPreferences() {
    AudioEngineHost.start()
    TempData.deviceName.value = Prefs.get<String>(PrefKeys.DEVICE_NAME)
    NetworkState.refresh()
    ChannelCacher.load()
    ChatCacher.load()
    LogCat.d("initCommonPreferences: clientId=${TempData.clientId}, deviceName=${TempData.deviceName.value}")
}

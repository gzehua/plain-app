package com.ismartcoding.plain.app

import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.features.chat.ChatCacher
import com.ismartcoding.plain.features.chat.channel.ChannelCacher
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.preferences.*

/**
 * Shared preference and TempData initialization, called by both Android
 * (`MainAppHelper`) and iOS (`MainViewController`) during app startup.
 *
 * Platform-specific initialization (Android: media duration cache, etc.)
 * stays in the platform modules and is called before
 * or after this function as needed.
 */
suspend fun initCommonPreferences() {
    com.ismartcoding.plain.features.videos.VideoProgressHost.restore()
    com.ismartcoding.plain.rust.host.media.AudioEngineHost.start()
    TempData.deviceName.value = UserPrefs.deviceName.value
    com.ismartcoding.plain.ui.app.LocalDeviceState.refresh()
    ChannelCacher.load()
    ChatCacher.load()
    LogCat.d("initCommonPreferences: clientId=${TempData.clientId}, deviceName=${TempData.deviceName.value}")
}

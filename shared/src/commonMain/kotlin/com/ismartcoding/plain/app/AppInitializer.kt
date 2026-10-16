package com.ismartcoding.plain.app

import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.features.chat.ChatCacher
import com.ismartcoding.plain.features.chat.channel.ChannelCacher
import com.ismartcoding.plain.ui.chat.peer.PeerCacher
import com.ismartcoding.plain.app.events.StartNearbyServiceEvent
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.features.dlna.DlnaRendererState
import com.ismartcoding.plain.preferences.*

/**
 * Shared preference and TempData initialization, called by both Android
 * (`MainAppHelper`) and iOS (`MainViewController`) during app startup.
 *
 * Platform-specific initialization (Android: media duration cache, PeerCacher,
 * FeedFetchWorker, etc.) stays in the platform modules and is called before
 * or after this function as needed.
 */
suspend fun initCommonPreferences() {
    com.ismartcoding.plain.features.videos.VideoProgressHost.restore()
    com.ismartcoding.plain.features.audio.AudioEngineHost.start()
    TempData.deviceName.value = UserPrefs.deviceName.value
    PeerCacher.load()
    ChannelCacher.load()
    ChatCacher.load()
    sendEvent(StartNearbyServiceEvent())
    if (TempData.canDLNAAccess()) {
        DlnaRendererState.start()
    }
    LogCat.d("initCommonPreferences: clientId=${TempData.clientId}, deviceName=${TempData.deviceName.value}")
}

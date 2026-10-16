package com.ismartcoding.plain.ui.page.playlist.components

import com.ismartcoding.plain.graphql.models.Audio as AudioTrack
import com.ismartcoding.plain.features.audio.AudioQueueManager
import com.ismartcoding.plain.graphql.models.AudioItem
import com.ismartcoding.plain.lib.TimeHelper
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.platform.audioJustPlayWithNotificationCheck
import com.ismartcoding.plain.ui.models.AudioQueueViewModel

internal suspend fun playPlaylistFrom(playlistId: String, startMediaId: String?, audioQueueVM: AudioQueueViewModel) {
    val start = withIO { run {
        AudioQueueManager.clearQueue()
        AudioQueueManager.add("playlist_id:$playlistId")
        val items = AudioQueueManager.queuePage(0, Int.MAX_VALUE)
        items.firstOrNull { it.mediaId == startMediaId } ?: items.firstOrNull()
    } }
    if (start != null) {
        audioJustPlayWithNotificationCheck(start)
        audioQueueVM.onStarted(start)
    }
}

internal suspend fun playPlaylistShuffled(playlistId: String, audioQueueVM: AudioQueueViewModel) {
    withIO { AudioQueueManager.clearQueue(); AudioQueueManager.add("playlist_id:$playlistId") }
    val next = withIO { AudioQueueManager.resolveNext(isNext = true, shuffle = true) }
    if (next != null) {
        audioJustPlayWithNotificationCheck(next)
        audioQueueVM.onStarted(next)
    }
}

internal fun AudioItem.toAudioTrack(): AudioTrack = AudioTrack(
    id = requireNotNull(mediaId),
    title = title,
    artist = artist,
    path = path,
    durationMs = durationMs,
    size = 0,
    bucketId = "",
    albumId = "",
    createdAt = TimeHelper.now(),
    updatedAt = TimeHelper.now(),
)

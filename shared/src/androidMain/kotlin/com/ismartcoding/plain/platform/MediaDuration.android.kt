package com.ismartcoding.plain.platform

import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.features.MediaDurationHelper
import com.ismartcoding.plain.events.MediaDurationZeroItem
import com.ismartcoding.plain.helpers.Mp4Helper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.audio.AudioMediaStoreHelper
import com.ismartcoding.plain.features.media.VideoMediaStoreHelper

actual suspend fun processSingleDurationZero(
    mediaType: String,
    item: MediaDurationZeroItem,
) {
    try {
        val file = java.io.File(item.path)
        val sizeBytes = file.length()
        val modifiedAtMs = file.lastModified()
        val durationMs = Mp4Helper.getMp4DurationMs(item.path)
        if (durationMs <= 0) return
        val stillCurrent = when (mediaType) {
            "audio" -> AudioMediaStoreHelper.isCurrentItemAsync(appContext, item)
            "video" -> VideoMediaStoreHelper.isCurrentItemAsync(appContext, item)
            else -> false
        }
        if (!stillCurrent || file.length() != sizeBytes || file.lastModified() != modifiedAtMs) return
        MediaDurationHelper.save(mediaType, item.id, durationMs, item.path, item.sizeBytes, item.modifiedAt)
        LogCat.d("Cached duration for $mediaType ${item.id}: ${durationMs}ms")
    } catch (e: Exception) {
        LogCat.e("Failed to cache duration for ${item.path}: ${e.message}")
    }
}

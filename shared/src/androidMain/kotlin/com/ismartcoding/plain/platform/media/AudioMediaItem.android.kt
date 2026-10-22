package com.ismartcoding.plain.platform.media

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.lib.extensions.pathToUri

@OptIn(UnstableApi::class)
internal fun AudioItem.toMediaItem(): MediaItem {
    val mediaMetadata = MediaMetadata.Builder()
        .setTitle(title)
        .setSubtitle(artist)
        .setArtist(artist)
        .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
        .build()

    return MediaItem.Builder()
        .setUri(path.pathToUri())
        .setMediaId(path)
        .setCustomCacheKey(path)
        .setMediaMetadata(mediaMetadata)
        .build()
}

package com.ismartcoding.plain.ui.platform.media

import androidx.compose.ui.graphics.ImageBitmap
import com.ismartcoding.plain.features.media.MediaClient

suspend fun loadAudioCoverBitmap(path: String): ImageBitmap? = try {
    val cached = MediaClient.cacheAudioCover(path)
    cached?.let(::decodeImageFileBitmap)
} catch (error: kotlinx.coroutines.CancellationException) {
    throw error
} catch (error: Exception) {
    com.ismartcoding.plain.lib.logcat.LogCat.e("Audio cover load failed: ${error.message}")
    null
}

expect fun audioAlbumArtUri(albumId: String): String

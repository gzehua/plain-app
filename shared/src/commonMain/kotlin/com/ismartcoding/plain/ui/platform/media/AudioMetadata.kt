package com.ismartcoding.plain.ui.platform.media

import androidx.compose.ui.graphics.ImageBitmap
import com.ismartcoding.plain.features.media.MediaClient
import com.ismartcoding.plain.lib.logcat.LogCat
import kotlinx.coroutines.CancellationException

suspend fun loadAudioCoverBitmap(path: String): ImageBitmap? = try {
    val cached = MediaClient.cacheAudioCover(path)
    cached?.let(::decodeImageFileBitmap)
} catch (error: CancellationException) {
    throw error
} catch (error: Exception) {
    LogCat.e("Audio cover load failed: ${error.message}")
    null
}

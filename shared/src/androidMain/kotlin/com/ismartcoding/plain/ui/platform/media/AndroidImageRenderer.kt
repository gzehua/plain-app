package com.ismartcoding.plain.ui.platform.media

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import com.ismartcoding.plain.platform.isSPlus
import com.ismartcoding.plain.ui.platform.system.ImageDecoder

import android.graphics.BitmapRegionDecoder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntSize
import com.ismartcoding.plain.ui.media.viewer.RawGesture
import com.ismartcoding.plain.ui.media.viewer.SizeChangeContent
import com.ismartcoding.plain.ui.platform.system.MediaHugeImage
import com.ismartcoding.plain.features.media.ImageType
import com.ismartcoding.plain.platform.media.ImageHelper
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.lib.extensions.isUrl
import com.ismartcoding.plain.ui.media.viewer.PreviewItem
import com.ismartcoding.plain.lib.logcat.LogCat
import java.io.IOException
import java.io.File
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

@Composable
private fun rememberImageDecoder(
    inputStream: InputStream,
    rotation: Int,
): ImageDecoder? {
    var imageDecoder by remember(inputStream) { mutableStateOf<ImageDecoder?>(null) }
    LaunchedEffect(inputStream) {
        var loaded: ImageDecoder? = null
        try {
            imageDecoder = withIO {
                inputStream.use {
                    val decoder = if (isSPlus()) BitmapRegionDecoder.newInstance(it)
                        else BitmapRegionDecoder.newInstance(it, false)
                    decoder?.let { value -> ImageDecoder(decoder = value, rotation = rotation) }
                        .also { value -> loaded = value }
                }
            }
            loaded = null
        } catch (error: IOException) {
            LogCat.e("Tiled image decode failed", error)
        } finally {
            loaded?.release()
        }
    }
    DisposableEffect(imageDecoder) {
        val decoder = imageDecoder
        onDispose { decoder?.release() }
    }
    return imageDecoder
}

private val fileExistsCache = ConcurrentHashMap<String, Boolean>()

@Composable
private fun rememberTiledImageDecoder(item: PreviewItem): ImageDecoder? {
    if (item.isVideo() || item.path.isUrl()) return null
    if (item.size <= 2000 * 1000) return null

    val imageType = remember(item.path) { ImageHelper.getImageType(item.path, item.path) }
    if (imageType.isApplicableAnimated() || imageType == ImageType.SVG) return null

    var rotation by remember(item.path) { mutableStateOf(item.rotation) }
    LaunchedEffect(item.path) {
        if (rotation == -1) {
            item.initImageAsync()
            rotation = item.rotation.takeIf { it >= 0 } ?: 0
        }
    }
    if (rotation == -1) return null
    val fileExists = remember(item.path) { fileExistsCache.getOrPut(item.path) { File(item.path).exists() } }
    if (!fileExists) return null

    val inputStream = remember(item.path) {
        try { File(item.path).inputStream() } catch (e: Exception) { fileExistsCache.remove(item.path); null }
    }
    val decoder = if (inputStream != null) rememberImageDecoder(inputStream = inputStream, rotation = rotation) else null
    if (decoder != null) item.intrinsicSize = IntSize(decoder.decoderWidth, decoder.decoderHeight)
    return decoder
}

internal object AndroidImageRenderer : ImageRenderer {
    @Composable
    override fun Content(
        model: PreviewItem,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        rotation: Float,
        gesture: RawGesture,
        onSizeChange: suspend (SizeChangeContent) -> Unit,
        onMounted: () -> Unit,
        boundClip: Boolean,
    ) {
        val decoder = rememberTiledImageDecoder(model)
        if (decoder != null) {
            MediaHugeImage(
                imageDecoder = decoder, scale = scale, offsetX = offsetX, offsetY = offsetY,
                rotation = rotation, gesture = gesture, onSizeChange = onSizeChange,
                onMounted = onMounted, boundClip = boundClip,
            )
        } else {
            MediaNormalImage(
                model = model, scale = scale, offsetX = offsetX, offsetY = offsetY,
                rotation = rotation, gesture = gesture, onSizeChange = onSizeChange,
                onMounted = onMounted, boundClip = boundClip,
            )
        }
    }
}

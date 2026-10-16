package com.ismartcoding.plain.ui.platform.media

import com.ismartcoding.plain.ui.platform.system.ImageDecoder
import com.ismartcoding.plain.ui.platform.system.MediaHugeImage

import androidx.compose.runtime.Composable
import com.ismartcoding.plain.ui.media.viewer.RawGesture
import com.ismartcoding.plain.ui.media.viewer.SizeChangeContent

@Composable
actual fun MediaHugeImageContent(
    model: Any?,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    rotation: Float,
    gesture: RawGesture,
    onSizeChange: suspend (SizeChangeContent) -> Unit,
    onMounted: () -> Unit,
    boundClip: Boolean,
) {
    if (model is ImageDecoder) {
        MediaHugeImage(
            imageDecoder = model,
            scale = scale,
            offsetX = offsetX,
            offsetY = offsetY,
            rotation = rotation,
            gesture = gesture,
            onSizeChange = onSizeChange,
            onMounted = onMounted,
            boundClip = boundClip,
        )
    }
}

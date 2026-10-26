package com.ismartcoding.plain.ui.platform.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.ismartcoding.plain.ui.media.viewer.PreviewItem
import com.ismartcoding.plain.ui.media.viewer.RawGesture
import com.ismartcoding.plain.ui.media.viewer.SizeChangeContent

internal val LocalImageRenderer = staticCompositionLocalOf<ImageRenderer?> { null }

internal interface ImageRenderer {
    @Composable
    fun Content(
        model: PreviewItem,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        rotation: Float,
        gesture: RawGesture,
        onSizeChange: suspend (SizeChangeContent) -> Unit,
        onMounted: () -> Unit,
        boundClip: Boolean,
    )
}

package com.ismartcoding.plain.ui.images.editor.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.ismartcoding.plain.lib.yjs.CanvasSize
import com.ismartcoding.plain.lib.yjs.EditorLayer

data class SceneViewport(
    val scale: Float,
    val panX: Float,
    val panY: Float,
)

data class SceneState(
    val canvasSize: CanvasSize,
    val bgColor: String,
    val layers: List<EditorLayer>,
    val viewport: SceneViewport,
    val sourceImagePath: String? = null,
    val imgOffsetX: Double = 0.0,
    val imgOffsetY: Double = 0.0,
    val imgAlpha: Double = 100.0,
    val images: Map<String, String> = emptyMap(),
    val hideLayerId: String? = null,
)

internal val LocalEditorCanvas = staticCompositionLocalOf<(@Composable (SceneState, Modifier, () -> Unit) -> Unit)?> { null }

@Composable
fun NativeEditorCanvas(
    state: SceneState,
    modifier: Modifier,
    onReady: () -> Unit = {},
) {
    val renderer = LocalEditorCanvas.current
    if (renderer == null) SceneCanvas(state, modifier, onReady)
    else renderer(state, modifier, onReady)
}

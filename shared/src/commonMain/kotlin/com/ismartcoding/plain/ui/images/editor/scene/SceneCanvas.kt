package com.ismartcoding.plain.ui.images.editor.scene

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.rememberTextMeasurer
import coil3.compose.rememberAsyncImagePainter
import com.ismartcoding.plain.lib.yjs.ImageLayer

@Composable
internal fun SceneCanvas(state: SceneState, modifier: Modifier, onReady: () -> Unit) {
    val textMeasurer = rememberTextMeasurer()
    val sourcePainter = rememberAsyncImagePainter(state.sourceImagePath)
    val imagePainters = state.images.mapValues { (_, path) -> rememberAsyncImagePainter(path) }
    val currentOnReady by rememberUpdatedState(onReady)
    var ready by remember { mutableStateOf(false) }
    Canvas(modifier = modifier.onSizeChanged { size ->
        if (!ready && size.width > 0 && size.height > 0) {
            ready = true
            currentOnReady()
        }
    }) {
        val cw = state.canvasSize.width.toFloat()
        val ch = state.canvasSize.height.toFloat()
        if (cw <= 0f || ch <= 0f) return@Canvas
        translate(state.viewport.panX, state.viewport.panY) {
            scale(state.viewport.scale, pivot = Offset.Zero) {
                drawSceneBackground(state.bgColor, cw, ch)
                translate(state.imgOffsetX.toFloat(), state.imgOffsetY.toFloat()) {
                    with(sourcePainter) {
                        draw(Size(cw, ch), alpha = (state.imgAlpha / 100).toFloat().coerceIn(0f, 1f))
                    }
                }
                for (layer in state.layers) {
                    if (!layer.visible || layer.id == state.hideLayerId) continue
                    if (layer is ImageLayer) {
                        val painter: Painter = imagePainters[layer.id] ?: sourcePainter
                        rotate(layer.rotation.toFloat(), Offset(layer.x.toFloat(), layer.y.toFloat())) {
                            translate((layer.x - layer.w / 2).toFloat(), (layer.y - layer.h / 2).toFloat()) {
                                with(painter) {
                                    draw(Size(layer.w.toFloat(), layer.h.toFloat()), alpha = layer.opacity.toFloat().coerceIn(0f, 1f))
                                }
                            }
                        }
                    } else {
                        drawSceneLayer(layer, textMeasurer)
                    }
                }
            }
        }
    }
}

package com.ismartcoding.plain.ui.charts

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun LiveLineChart(
    samples: List<LiveChartSample>,
    isRunning: Boolean,
    modifier: Modifier = Modifier,
    playback: LiveChartPlayback = remember { LiveChartPlayback() },
    color: Color = MaterialTheme.colorScheme.primary,
    onVisibleHighlightChange: ((Boolean) -> Unit)? = null,
    selectedSampleIndex: Int? = null,
    onSampleSelected: ((LiveChartSample) -> Unit)? = null,
) {
    val highlightColor = MaterialTheme.colorScheme.error
    val currentSamples by rememberUpdatedState(samples)
    val currentOnSampleSelected by rememberUpdatedState(onSampleSelected)
    var reportedHighlight by remember(playback) { mutableStateOf(false) }
    LaunchedEffect(samples, playback) {
        samples.forEach { playback.append(it) }
    }
    LaunchedEffect(isRunning, playback) {
        if (!isRunning) return@LaunchedEffect
        var previous = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                playback.advance((now - previous) / 1_000_000_000.0)
                previous = now
            }
        }
    }
    Box(modifier.pointerInput(playback, onSampleSelected != null) {
        if (onSampleSelected == null) return@pointerInput
        detectTapGestures { offset ->
            val step = size.width.toFloat() / playback.visibleIntervals
            playback.points.withIndex()
                .filter { point -> currentSamples.any { it.index == point.value.index } }
                .minByOrNull { point ->
                    abs(size.width + (point.index - playback.points.lastIndex + 1 - playback.phase) * step - offset.x)
                }?.let { currentOnSampleSelected?.invoke(it.value) }
        }
    }.drawWithCache {
        val line = Path()
        val area = Path()
        val fill = Brush.verticalGradient(listOf(color.copy(alpha = 0.2f), Color.Transparent))
        val stroke = Stroke(width = 2.dp.toPx())
        val step = size.width / playback.visibleIntervals
        val highlightWidth = 8.dp.toPx()
        val baseline = size.height - 4.dp.toPx()
        val top = 4.dp.toPx()
        onDrawBehind {
            val points = playback.points
            if (points.size < 2) return@onDrawBehind
            line.rewind()
            var previous = Offset.Zero
            for (index in points.indices) {
                val point = points[index]
                val next = Offset(
                    size.width + (index - points.lastIndex + 1 - playback.phase) * step,
                    baseline - (baseline - top) * (point.value / playback.yMax).coerceIn(0f, 1f),
                )
                if (index == 0) line.moveTo(next.x, next.y)
                else {
                    val middle = (previous.x + next.x) / 2f
                    line.cubicTo(middle, previous.y, middle, next.y, next.x, next.y)
                }
                previous = next
            }
            area.rewind()
            area.addPath(line)
            area.lineTo(previous.x, baseline)
            area.lineTo(size.width + (1 - points.lastIndex - playback.phase) * step, baseline)
            area.close()
            clipRect {
                drawPath(area, fill)
                drawPath(line, color, style = stroke)
                var visibleHighlight = false
                points.forEachIndexed { index, point ->
                    val x = size.width + (index - points.lastIndex + 1 - playback.phase) * step
                    if (point.highlighted && x + highlightWidth / 2f > 0f) {
                        visibleHighlight = true
                        drawRect(highlightColor, Offset(x - highlightWidth / 2f, 0f), Size(highlightWidth, size.height))
                    }
                    if (point.index == selectedSampleIndex) {
                        drawCircle(color, 4.dp.toPx(), Offset(x, baseline - (baseline - top) * (point.value / playback.yMax).coerceIn(0f, 1f)))
                    }
                }
                if (visibleHighlight != reportedHighlight) {
                    reportedHighlight = visibleHighlight
                    onVisibleHighlightChange?.invoke(visibleHighlight)
                }
            }
        }
    })
}

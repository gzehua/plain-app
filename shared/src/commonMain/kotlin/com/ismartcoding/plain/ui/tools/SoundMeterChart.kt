package com.ismartcoding.plain.ui.tools

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.abs

internal data class SoundMeterSample(val seconds: Float, val decibels: Float)

@Composable
internal fun SoundMeterChart(samples: List<SoundMeterSample>, selected: SoundMeterSample?, onSelect: (SoundMeterSample) -> Unit, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    val scale = maxOf(100f, ceil((samples.maxOfOrNull { it.decibels } ?: 0f) / 20f) * 20f)
    Canvas(modifier.pointerInput(samples) {
        detectTapGestures { offset ->
            if (samples.isNotEmpty()) {
                val time = samples.last().seconds - 12f * (1f - offset.x / size.width)
                samples.minByOrNull { abs(it.seconds - time) }?.let(onSelect)
            }
        }
    }) {
        if (samples.size < 2) return@Canvas
        val lastTime = samples.last().seconds
        val top = 4.dp.toPx()
        val baseline = size.height - top
        val line = Path()
        samples.forEachIndexed { index, sample ->
            val x = size.width * (1f - (lastTime - sample.seconds) / 12f)
            val y = baseline - (baseline - top) * (sample.decibels / scale).coerceIn(0f, 1f)
            if (index == 0) line.moveTo(x, y) else line.lineTo(x, y)
        }
        val area = Path().apply {
            addPath(line)
            lineTo(size.width, baseline)
            lineTo(size.width * (1f - (lastTime - samples.first().seconds) / 12f), baseline)
            close()
        }
        drawPath(area, Brush.verticalGradient(listOf(color.copy(alpha = 0.2f), Color.Transparent)))
        drawPath(line, color, style = Stroke(width = 2.dp.toPx()))
        if (selected != null && selected in samples) {
            drawCircle(color, 4.dp.toPx(), androidx.compose.ui.geometry.Offset(
                size.width * (1f - (lastTime - selected.seconds) / 12f),
                baseline - (baseline - top) * (selected.decibels / scale).coerceIn(0f, 1f),
            ))
        }
    }
}

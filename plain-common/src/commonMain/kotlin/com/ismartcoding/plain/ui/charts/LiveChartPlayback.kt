package com.ismartcoding.plain.ui.charts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

data class LiveChartSample(val index: Int, val value: Float, val highlighted: Boolean = false)

class LiveChartPlayback(
    private val capacity: Int = 12,
    private val sampleIntervalSeconds: Double = 1.0,
    private val minimumYMax: Float = 1f,
) {
    init {
        require(capacity >= 3)
        require(sampleIntervalSeconds.isFinite() && sampleIntervalSeconds > 0.0)
        require(minimumYMax.isFinite() && minimumYMax > 0f)
    }

    private val history = mutableStateListOf<LiveChartSample>()
    val points: List<LiveChartSample> get() = history
    val visibleIntervals: Int get() = capacity - 2
    private val pending = ArrayDeque<LiveChartSample>()
    var phase by mutableFloatStateOf(1f)
        private set
    var yMax by mutableFloatStateOf(minimumYMax)
        private set
    private var clock = 0.0
    private var segmentStart = 0.0
    private var duration = sampleIntervalSeconds * 1.15
    private var interval = sampleIntervalSeconds
    private var lastArrival: Double? = null
    var lastSampleIndex = -1
        private set
    private var targetYMax = minimumYMax
    private var playing = false

    fun append(sample: LiveChartSample) {
        if (sample.index <= lastSampleIndex) return
        lastSampleIndex = sample.index
        if (history.isEmpty()) {
            repeat(capacity - 1) { offset ->
                history.add(LiveChartSample(sample.index - capacity + 1 + offset, 0f))
            }
        }
        lastArrival?.let { previous ->
            val gap = clock - previous
            if (gap in 0.05..5.0) interval = interval * 0.9 + gap * 0.1
        }
        lastArrival = clock
        pending.addLast(sample)
        while (pending.size > capacity) commit(pending.removeFirst())
        if (!playing) startSegment(clock)
        else if (pending.size == 1 && (clock - segmentStart) / duration > 0.85) {
            duration = interval
            segmentStart = clock - phase * duration
        }
    }

    fun advance(seconds: Double) {
        clock += seconds.coerceAtLeast(0.0)
        if (!playing) return
        while (pending.isNotEmpty() && clock - segmentStart >= duration) {
            startSegment(segmentStart + duration)
        }
        val raw = ((clock - segmentStart) / duration).coerceAtLeast(0.0)
        phase = if (pending.isNotEmpty()) raw.coerceAtMost(1.0).toFloat()
        else if (raw <= 0.85) raw.toFloat()
        else (1.0 - 0.15 * exp(-(raw - 0.85) / 0.15)).toFloat()
        if (pending.isEmpty() && raw >= 3.0) {
            phase = 1f
            playing = false
        }
        val blend = (1.0 - exp(-seconds.coerceAtLeast(0.0) * 5.0)).toFloat()
        yMax += (targetYMax - yMax) * blend
    }

    fun clear() {
        history.clear()
        pending.clear()
        phase = 1f
        yMax = minimumYMax
        clock = 0.0
        segmentStart = 0.0
        duration = sampleIntervalSeconds * 1.15
        interval = sampleIntervalSeconds
        lastArrival = null
        lastSampleIndex = -1
        targetYMax = minimumYMax
        playing = false
    }

    private fun startSegment(at: Double) {
        while (pending.size > 2) commit(pending.removeFirst())
        commit(pending.removeFirst())
        duration = interval * if (pending.isEmpty()) 1.15 else 0.9
        segmentStart = at
        phase = 0f
        playing = true
    }

    private fun commit(sample: LiveChartSample) {
        history.add(sample)
        while (history.size > capacity) history.removeAt(0)
        targetYMax = maxOf(minimumYMax, niceCeil(history.maxOf { it.value }))
    }
}

private fun niceCeil(value: Float): Float {
    if (value <= 0f) return 1f
    val base = 10f.pow(floor(log10(value)))
    val scaled = value / base
    return (NICE_STEPS.firstOrNull { it >= scaled } ?: 10f) * base
}

private val NICE_STEPS = floatArrayOf(1f, 1.5f, 2f, 2.5f, 3f, 4f, 5f, 6f, 8f, 10f)

package com.ismartcoding.plain.ui.charts

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiveChartPlaybackTest {
    private fun sample(index: Int) = LiveChartSample(index, index.toFloat())

    @Test
    fun highlightStaysWithItsSampleAndDoesNotMarkSyntheticHistory() {
        val chart = LiveChartPlayback()
        val loss = LiveChartSample(0, 5f, highlighted = true)
        chart.append(loss)
        assertEquals(listOf(loss), chart.points.filter { it.highlighted })
        assertTrue(chart.points.dropLast(1).none { it.highlighted })
        chart.advance(1.0)
        chart.append(LiveChartSample(1, 10f))
        chart.advance(1.0)
        assertEquals(listOf(loss), chart.points.filter { it.highlighted })
        repeat(20) {
            chart.append(LiveChartSample(it + 2, 10f))
            chart.advance(1.0)
        }
        assertTrue(chart.points.none { it.highlighted })
    }

    @Test
    fun firstSampleEntersFromAZeroBaselineSpanningTheWholePlot() {
        val chart = LiveChartPlayback()
        val first = LiveChartSample(0, 80f)
        chart.append(first)
        assertEquals(12, chart.points.size)
        assertTrue(chart.points.dropLast(1).all { point -> point.value == 0f })
        assertEquals(first, chart.points.last())
        assertEquals(0f, chart.phase)
        assertEquals(-10f, 0 - chart.points.lastIndex + 1f - chart.phase)
        chart.advance(0.5)
        assertTrue(chart.phase > 0f)
        assertTrue(chart.phase < 1f)
        assertEquals(first, chart.points.last())
    }

    @Test
    fun lateSampleDoesNotResetTheDisplayedPhase() {
        val chart = LiveChartPlayback()
        chart.append(sample(0))
        chart.advance(1.2)
        val phase = chart.phase
        chart.append(sample(1))
        assertEquals(phase, chart.phase)
        chart.advance(0.01)
        assertTrue(chart.phase >= phase)
    }

    @Test
    fun newSegmentPreservesThePositionOfExistingPoints() {
        val chart = LiveChartPlayback()
        chart.append(sample(0))
        chart.advance(1.0)
        chart.append(sample(1))
        chart.advance(1.0 - chart.phase - 0.000001)
        val oldX = 1f - chart.phase
        chart.advance(0.000001)
        val retainedIndex = chart.points.indexOfFirst { it.index == 0 }
        val newX = retainedIndex - chart.points.lastIndex + 1f - chart.phase
        assertEquals(oldX, newX, 0.00001f)
    }

    @Test
    fun delayedArrivalSlowsSmoothlyWithoutReversing() {
        val chart = LiveChartPlayback()
        chart.append(sample(0))
        var lastPhase = 0f
        repeat(300) {
            chart.advance(0.01)
            assertTrue(chart.phase >= lastPhase)
            assertTrue(chart.phase <= 1f)
            lastPhase = chart.phase
        }
    }

    @Test
    fun longRunningAndBurstyStreamsKeepBoundedHistory() {
        val chart = LiveChartPlayback()
        repeat(10_000) { chart.append(sample(it)) }
        chart.advance(30.0)
        assertTrue(chart.points.size <= 12)
        assertEquals(9_999, chart.points.last().index)
        assertTrue(chart.yMax.isFinite())
    }

    @Test
    fun pausedPlaybackKeepsItsPositionAndIgnoresAlreadyQueuedSamples() {
        val chart = LiveChartPlayback()
        chart.append(sample(0))
        chart.advance(0.4)
        val phase = chart.phase
        val points = chart.points.toList()
        repeat(100) { chart.append(sample(0)) }
        assertEquals(phase, chart.phase)
        assertEquals(points, chart.points.toList())
        chart.advance(0.1)
        assertTrue(chart.phase > phase)
    }

    @Test
    fun audioSamplesKeepMovingBetweenArrivalsAtFrameRate() {
        val chart = LiveChartPlayback(capacity = 68, sampleIntervalSeconds = 0.18, minimumYMax = 100f)
        chart.append(LiveChartSample(0, 60f))
        val positions = mutableSetOf<Float>()
        repeat(10) {
            chart.advance(1.0 / 60)
            positions.add(chart.phase)
        }
        assertEquals(10, positions.size)
        val beforeArrival = chart.phase
        chart.append(LiveChartSample(1, 80f))
        assertEquals(beforeArrival, chart.phase)
        assertEquals(100f, chart.yMax)
    }

    @Test
    fun clearDropsOldAudioAndAcceptsRestartedSampleIndices() {
        val chart = LiveChartPlayback(capacity = 68, sampleIntervalSeconds = 0.18, minimumYMax = 100f)
        chart.append(LiveChartSample(20, 110f))
        chart.advance(0.1)
        chart.clear()
        assertTrue(chart.points.isEmpty())
        assertEquals(-1, chart.lastSampleIndex)
        assertEquals(100f, chart.yMax)
        chart.append(LiveChartSample(0, 40f))
        assertEquals(40f, chart.points.last().value)
        assertTrue(chart.points.dropLast(1).all { it.value == 0f })
    }
}

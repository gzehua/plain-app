package com.ismartcoding.plain.ui.platform.media

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SoundMeterSamplingTest {
    @Test
    fun readsAndPublishesOnlyOncePerSecond() = runTest {
        var reads = 0
        val samples = mutableListOf<Float>()
        val job = launch { sampleSoundMeter({ reads++; 60f }) { samples.add(it) } }
        runCurrent()
        assertEquals(1, reads)
        advanceTimeBy(999)
        runCurrent()
        assertEquals(1, reads)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(2, reads)
        assertEquals(listOf(60f, 60f), samples)
        job.cancel()
        advanceTimeBy(3000)
        assertEquals(2, reads)
    }

    @Test
    fun invalidReadingsWaitForTheNextSampleWithoutPublishing() = runTest {
        val readings = ArrayDeque(listOf(Float.NaN, 0f, -1f, 60f))
        val samples = mutableListOf<Float>()
        val job = launch { sampleSoundMeter({ readings.removeFirst() }) { samples.add(it) } }
        advanceTimeBy(3000)
        runCurrent()
        assertEquals(listOf(60f), samples)
        job.cancel()
    }
}

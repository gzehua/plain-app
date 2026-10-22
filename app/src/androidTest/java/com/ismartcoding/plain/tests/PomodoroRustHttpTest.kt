package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.features.pomodoro.PomodoroClient
import com.ismartcoding.plain.models.PomodoroState

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PomodoroRustHttpTest {
    @Test
    fun hostControlsAuthoritativeRustTimerWithoutCompletingARealSession() = runBlocking {
        val original = PomodoroClient.today()
        assumeTrue(!original.isRunning && !original.isPaused && original.state == PomodoroState.WORK && original.timeLeftSec == original.totalTimeSec)
        try {
            val started = PomodoroClient.start(30)
            assertTrue(started.isRunning)
            assertFalse(started.isPaused)
            assertTrue(started.timeLeftSec in 1..30)
            val paused = PomodoroClient.pause()
            assertFalse(paused.isRunning)
            assertTrue(paused.isPaused)
            val same = PomodoroClient.today()
            assertEquals(paused.timeLeftSec, same.timeLeftSec)
            assertEquals(original.completedCount, same.completedCount)
            val resumed = PomodoroClient.start(same.timeLeftSec)
            assertTrue(resumed.isRunning)
            assertFalse(resumed.isPaused)
            assertEquals(original.completedCount, resumed.completedCount)
            val stopped = PomodoroClient.stop()
            assertFalse(stopped.isRunning)
            assertFalse(stopped.isPaused)
            assertEquals(PomodoroState.WORK, stopped.state)
            assertEquals(stopped.totalTimeSec, stopped.timeLeftSec)
        } finally {
            PomodoroClient.stop()
        }
    }
}

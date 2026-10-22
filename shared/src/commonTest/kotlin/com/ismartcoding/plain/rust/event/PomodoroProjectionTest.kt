package com.ismartcoding.plain.rust.event

import com.ismartcoding.plain.models.PomodoroState
import com.ismartcoding.plain.models.PomodoroToday
import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.features.pomodoro.PomodoroClient.today
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineStart
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.app.events.HPomodoroChangedEvent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PomodoroProjectionTest {
    private val snapshot = """{"date":"2026-10-15T16:00:00.000Z","completedCount":2,"currentRound":3,"timeLeftSec":120,"totalTimeSec":300,"isRunning":true,"isPaused":false,"state":"SHORT_BREAK"}"""

    @Test fun progress_events_project_without_querying_a_running_core() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val received = mutableListOf<PomodoroToday>()
        val collector = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            Channel.sharedFlow.collect { if (it is HPomodoroChangedEvent) received.add(it.today) }
        }
        try {
        repeat(32) {
            RustEventProjection.apply(RustEvent(
                EventType.POMODORO_ACTION.name,
                """{"action":"progress","today":$snapshot,"completedState":null}""",
            ))
        }
        runCurrent()
        assertEquals(32, received.size)
        assertTrue(received.all { it.timeLeftSec == 120 && it.currentRound == 3 })
        } finally {
            collector.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test fun api_and_event_snapshots_share_the_canonical_model() {
        val element = JsonHelper.jsonDecode<JsonElement>(snapshot)
        val today: PomodoroToday = element.today()
        assertEquals(2, today.completedCount)
        assertEquals(3, today.currentRound)
        assertEquals(120, today.timeLeftSec)
        assertEquals(PomodoroState.SHORT_BREAK, today.state)
        assertFalse(today.isPaused)
        assertEquals(today, JsonHelper.jsonDecode<PomodoroToday>(JsonHelper.jsonEncode(today)))
    }
}

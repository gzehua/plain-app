package com.ismartcoding.plain.rust

import com.ismartcoding.plain.ui.pomodoro.PomodoroProjection
import com.ismartcoding.plain.models.PomodoroState
import com.ismartcoding.plain.models.PomodoroToday
import com.ismartcoding.plain.api.graphql.models.PomodoroAction
import com.ismartcoding.plain.features.pomodoro.PomodoroClient.today
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PomodoroEventsTest {
    private val snapshot = """{"date":"2026-10-15T16:00:00.000Z","completedCount":2,"currentRound":3,"timeLeftSec":120,"totalTimeSec":300,"isRunning":true,"isPaused":false,"state":"SHORT_BREAK"}"""

    @Test fun progress_events_update_state_without_querying_a_running_core() = runTest {
        val previous = PomodoroProjection.today.value
        PomodoroProjection.today.value = null
        try {
            repeat(32) {
                PomodoroProjection.apply(PomodoroAction(JsonHelper.jsonDecode<PomodoroToday>(snapshot)))
                assertEquals(JsonHelper.jsonDecode<PomodoroToday>(snapshot), PomodoroProjection.today.value)
            }
        } finally {
            PomodoroProjection.today.value = previous
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

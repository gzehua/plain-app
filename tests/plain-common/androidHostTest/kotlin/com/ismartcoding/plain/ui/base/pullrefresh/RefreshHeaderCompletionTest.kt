package com.ismartcoding.plain.ui.base.pullrefresh

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class RefreshHeaderCompletionTest {
    @Test
    fun a_long_pull_releases_one_refresh_and_restores_content_after_success_or_failure() {
        listOf(RefreshContentState.Finished, RefreshContentState.Failed).forEach { result ->
            val clock = AdvancingFrameClock()
            runTest(clock) {
                var calls = 0
                val state = RefreshLayoutState { calls++ }.also {
                    it.coroutineScope = CoroutineScope(StandardTestDispatcher(testScheduler) + clock)
                    it.refreshContentThresholdState.floatValue = 120f
                }
                val connection = RefreshLayoutNestedScrollConnection(ComposePosition.Top, state, 0.5f, false)
                connection.onPostScroll(Offset.Zero, Offset(0f, 5000f), NestedScrollSource.UserInput)
                runCurrent()
                assertEquals(120f, state.getRefreshContentOffset())
                connection.onPreFling(Velocity.Zero)
                advanceUntilIdle()
                assertEquals(1, calls)
                assertEquals(RefreshContentState.Refreshing, state.refreshContentState.value)
                assertEquals(120f, state.getRefreshContentOffset())
                state.setRefreshState(result)
                advanceUntilIdle()
                assertEquals(result, state.refreshContentState.value)
                assertEquals(0f, state.getRefreshContentOffset())
                assertEquals(1, calls)
            }
        }
    }

    @Test
    fun hidden_header_stays_hidden_when_the_active_request_finishes_or_fails() {
        listOf(RefreshContentState.Finished, RefreshContentState.Failed).forEach { result ->
            val clock = AdvancingFrameClock()
            runTest(clock) {
                var calls = 0
                val state = RefreshLayoutState { calls++ }.also {
                    it.coroutineScope = CoroutineScope(StandardTestDispatcher(testScheduler) + clock)
                    it.refreshContentThresholdState.floatValue = 120f
                }
                state.setRefreshState(RefreshContentState.Refreshing)
                advanceUntilIdle()
                assertEquals(120f, state.getRefreshContentOffset())
                val connection = RefreshLayoutNestedScrollConnection(ComposePosition.Top, state, 0.5f, false)
                assertEquals(Offset(0f, -120f),
                    connection.onPreScroll(Offset(0f, -400f), NestedScrollSource.UserInput))
                runCurrent()
                assertEquals(0f, state.getRefreshContentOffset())
                assertEquals(Velocity.Zero, connection.onPreFling(Velocity(0f, -2400f)))
                state.setRefreshState(RefreshContentState.Refreshing)
                advanceUntilIdle()
                assertEquals(1, calls)
                assertEquals(0f, state.getRefreshContentOffset())
                state.setRefreshState(result)
                advanceUntilIdle()
                assertEquals(result, state.refreshContentState.value)
                assertEquals(0f, state.getRefreshContentOffset())
                assertEquals(1, calls)
            }
        }
    }
}

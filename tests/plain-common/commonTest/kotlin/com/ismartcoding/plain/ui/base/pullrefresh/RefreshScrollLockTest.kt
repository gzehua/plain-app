package com.ismartcoding.plain.ui.base.pullrefresh

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlin.test.Test
import kotlin.test.assertEquals

class RefreshScrollLockTest {
    private suspend fun stateIn(
        position: ComposePosition = ComposePosition.Top,
        state: RefreshContentState = RefreshContentState.Refreshing,
        onRefresh: RefreshLayoutState.() -> Unit = {},
    ) = RefreshLayoutState(onRefresh).also {
        it.coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        it.composePositionState.value = position
        it.refreshContentThresholdState.floatValue = THRESHOLD
        it.refreshContentState.value = state
        it.refreshContentOffsetState.snapTo(sign(position) * THRESHOLD)
    }

    private fun connection(state: RefreshLayoutState, refreshingCanScroll: Boolean = false) =
        RefreshLayoutNestedScrollConnection(
            state.composePositionState.value, state, 0.5f,
            state.composePositionState.value.isHorizontal(), refreshingCanScroll,
        )

    @Test
    fun refreshing_header_scrolls_away_before_the_list_in_every_direction() = runTest {
        ComposePosition.entries.forEach { position ->
            val state = stateIn(position)
            val conn = connection(state)
            val available = axis(position, -sign(position) * 320f)
            assertEquals(axis(position, -sign(position) * THRESHOLD),
                conn.onPreScroll(available, NestedScrollSource.UserInput), position.name)
            assertEquals(0f, state.getRefreshContentOffset(), position.name)
            assertEquals(RefreshContentState.Refreshing, state.refreshContentState.value)
            assertEquals(Offset.Zero, conn.onPreScroll(available, NestedScrollSource.UserInput))
        }
    }

    @Test
    fun short_scroll_moves_refreshing_header_at_the_same_speed_as_content() = runTest {
        ComposePosition.entries.forEach { position ->
            val state = stateIn(position)
            val delta = axis(position, -sign(position) * 40f)
            assertEquals(delta, connection(state).onPreScroll(delta, NestedScrollSource.UserInput))
            assertEquals(sign(position) * 80f, state.getRefreshContentOffset())
            assertEquals(RefreshContentState.Refreshing, state.refreshContentState.value)
        }
    }

    @Test
    fun fling_scroll_can_finish_hiding_the_header_without_eating_list_velocity() = runTest {
        val state = stateIn()
        val conn = connection(state)
        assertEquals(Velocity.Zero, conn.onPreFling(Velocity(0f, -2400f)))
        assertEquals(Offset(0f, -THRESHOLD),
            conn.onPreScroll(Offset(0f, -320f), NestedScrollSource.SideEffect))
        assertEquals(0f, state.getRefreshContentOffset())
        assertEquals(RefreshContentState.Refreshing, state.refreshContentState.value)
    }

    @Test
    fun scrolling_back_at_the_edge_reveals_only_the_existing_refresh_header() = runTest {
        ComposePosition.entries.forEach { position ->
            val state = stateIn(position)
            state.refreshContentOffsetState.snapTo(0f)
            val conn = connection(state)
            val delta = axis(position, sign(position) * 320f)
            assertEquals(axis(position, sign(position) * THRESHOLD),
                conn.onPostScroll(Offset.Zero, delta, NestedScrollSource.UserInput))
            assertEquals(sign(position) * THRESHOLD, state.getRefreshContentOffset())
            assertEquals(Offset.Zero, conn.onPostScroll(Offset.Zero, delta, NestedScrollSource.UserInput))
            assertEquals(RefreshContentState.Refreshing, state.refreshContentState.value)
        }
    }

    @Test
    fun list_scroll_and_outward_fling_do_not_pull_the_header() = runTest {
        val state = stateIn()
        state.refreshContentOffsetState.snapTo(0f)
        val conn = connection(state)
        assertEquals(Offset.Zero, conn.onPreScroll(Offset(0f, 320f), NestedScrollSource.UserInput))
        assertEquals(Offset.Zero, conn.onPostScroll(Offset.Zero, Offset(0f, 320f), NestedScrollSource.SideEffect))
        assertEquals(0f, state.getRefreshContentOffset())
    }

    @Test
    fun scrolling_during_refresh_does_not_restart_the_request_or_finish_it() = runTest {
        var calls = 0
        val state = stateIn(onRefresh = { calls++ })
        state.runRefreshListener()
        val conn = connection(state)
        repeat(3) {
            conn.onPreScroll(Offset(0f, -320f), NestedScrollSource.UserInput)
            conn.onPreFling(Velocity(0f, -2400f))
            conn.onPostScroll(Offset.Zero, Offset(0f, 320f), NestedScrollSource.UserInput)
            conn.onPreFling(Velocity(0f, 2400f))
        }
        assertEquals(1, calls)
        assertEquals(RefreshContentState.Refreshing, state.refreshContentState.value)
    }

    @Test
    fun drag_retraction_never_consumes_more_than_the_visible_header() = runTest {
        ComposePosition.entries.forEach { position ->
            val state = stateIn(position, RefreshContentState.Dragging)
            val available = axis(position, -sign(position) * 320f)
            assertEquals(axis(position, -sign(position) * THRESHOLD / 0.5f),
                connection(state).onPreScroll(available, NestedScrollSource.UserInput))
            assertEquals(0f, state.getRefreshContentOffset())
        }
    }

    @Test
    fun a_long_edge_pull_never_moves_the_content_beyond_the_header() = runTest {
        ComposePosition.entries.forEach { position ->
            val state = stateIn(position, RefreshContentState.Finished)
            state.refreshContentOffsetState.snapTo(0f)
            val available = axis(position, sign(position) * 5000f)
            assertEquals(axis(position, sign(position) * THRESHOLD / 0.5f),
                connection(state).onPostScroll(Offset.Zero, available, NestedScrollSource.UserInput))
            assertEquals(sign(position) * THRESHOLD, state.getRefreshContentOffset())
            assertEquals(RefreshContentState.Dragging, state.refreshContentState.value)
        }
    }

    @Test
    fun dragging_past_the_header_repeatedly_does_not_accumulate_blank_space() = runTest {
        val state = stateIn(state = RefreshContentState.Dragging)
        val conn = connection(state)
        repeat(100) {
            assertEquals(Offset.Zero,
                conn.onPostScroll(Offset.Zero, Offset(0f, 80f), NestedScrollSource.UserInput))
            assertEquals(THRESHOLD, state.getRefreshContentOffset())
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun queued_touch_events_cannot_push_the_header_offscreen_or_past_its_own_edge() = runTest {
        ComposePosition.entries.forEach { position ->
            val state = stateIn(position, RefreshContentState.Finished)
            state.refreshContentOffsetState.snapTo(0f)
            state.coroutineScope = CoroutineScope(StandardTestDispatcher(testScheduler))
            val conn = connection(state)
            repeat(100) {
                conn.onPostScroll(Offset.Zero, axis(position, sign(position) * 80f), NestedScrollSource.UserInput)
            }
            runCurrent()
            assertEquals(sign(position) * THRESHOLD, state.getRefreshContentOffset())
            repeat(100) {
                conn.onPreScroll(axis(position, -sign(position) * 80f), NestedScrollSource.UserInput)
            }
            runCurrent()
            assertEquals(0f, state.getRefreshContentOffset())
        }
    }

    @Test
    fun normal_pull_still_uses_drag_resistance() = runTest {
        val state = stateIn(state = RefreshContentState.Finished)
        state.refreshContentOffsetState.snapTo(0f)
        val available = Offset(0f, 80f)
        assertEquals(available, connection(state).onPostScroll(Offset.Zero, available, NestedScrollSource.UserInput))
        assertEquals(40f, state.getRefreshContentOffset())
        assertEquals(RefreshContentState.Dragging, state.refreshContentState.value)
    }

    @Test
    fun load_more_can_still_move_past_threshold_without_changing_request_state() = runTest {
        val state = stateIn(ComposePosition.Bottom)
        val available = Offset(0f, -40f)
        assertEquals(available, connection(state, refreshingCanScroll = true)
            .onPostScroll(Offset.Zero, available, NestedScrollSource.UserInput))
        assertEquals(-140f, state.getRefreshContentOffset())
        assertEquals(RefreshContentState.Refreshing, state.refreshContentState.value)
    }

    private fun sign(position: ComposePosition) =
        if (position == ComposePosition.Top || position == ComposePosition.Start) 1f else -1f

    private fun axis(position: ComposePosition, value: Float) =
        if (position.isHorizontal()) Offset(value, 0f) else Offset(0f, value)

    private companion object {
        const val THRESHOLD = 120f
    }
}

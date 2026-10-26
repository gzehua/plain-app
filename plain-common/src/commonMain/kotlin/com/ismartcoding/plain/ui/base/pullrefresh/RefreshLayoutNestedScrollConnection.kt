package com.ismartcoding.plain.ui.base.pullrefresh

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs
import kotlin.math.min

internal class RefreshLayoutNestedScrollConnection(
    private val composePosition: ComposePosition,
    private val refreshLayoutState: RefreshLayoutState,
    private val dragEfficiency: Float,
    private val orientationIsHorizontal: Boolean,
    private val refreshingCanScroll: Boolean = false,
) : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val refreshing = refreshLayoutState.refreshContentState.value == RefreshContentState.Refreshing
        if (source != NestedScrollSource.UserInput && !refreshing) return Offset.Zero
        val offset = refreshLayoutState.getRefreshContentOffset()
        val delta = axisValue(available)
        if (offset * delta >= 0f) return Offset.Zero
        val efficiency = if (refreshing) 1f else dragEfficiency
        val consumed = if (offset > 0f) delta.coerceAtLeast(-offset / efficiency)
            else delta.coerceAtMost(-offset / efficiency)
        refreshLayoutState.offset(consumed * efficiency)
        return axisOffset(consumed)
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        if (source != NestedScrollSource.UserInput) return Offset.Zero
        val delta = axisValue(available)
        val direction = if (composePosition == ComposePosition.Top || composePosition == ComposePosition.Start) 1f else -1f
        if (delta * direction <= 0.01f) return Offset.Zero
        val refreshing = refreshLayoutState.refreshContentState.value == RefreshContentState.Refreshing
        val bounded = !refreshing || !refreshingCanScroll
        val efficiency = if (refreshing && bounded) 1f else dragEfficiency
        val maxOffset = if (bounded) refreshLayoutState.getRefreshContentThreshold() else Float.POSITIVE_INFINITY
        val remaining = (maxOffset - abs(refreshLayoutState.getRefreshContentOffset())).coerceAtLeast(0f)
        val used = direction * min(abs(delta), remaining / efficiency)
        if (used == 0f) return Offset.Zero
        refreshLayoutState.offset(used * efficiency, maxOffset = maxOffset)
        return axisOffset(used)
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (refreshLayoutState.refreshContentState.value == RefreshContentState.Refreshing) return Velocity.Zero
        if (refreshLayoutState.getRefreshContentOffset() != 0f) {
            refreshLayoutState.offsetHoming()
            return available
        }
        return Velocity.Zero
    }

    private fun axisValue(offset: Offset) = if (orientationIsHorizontal) offset.x else offset.y

    private fun axisOffset(value: Float) = if (orientationIsHorizontal) Offset(value, 0f) else Offset(0f, value)
}

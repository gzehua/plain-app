package com.ismartcoding.plain.ui.base.pullrefresh

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.*
import com.ismartcoding.plain.lib.logcat.LogCat
import kotlin.math.abs
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow

@Stable
class RefreshLayoutState(
    val onRefreshListener: RefreshLayoutState.() -> Unit
) {
    val refreshContentState = mutableStateOf(RefreshContentState.Finished)
    val refreshContentOffsetState = Animatable(0f)
    val composePositionState = mutableStateOf(ComposePosition.Top)
    val refreshContentThresholdState = mutableFloatStateOf(0f)

    val lastRefreshTimeState = mutableStateOf<Long?>(null)
    private var nowMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() }

    lateinit var coroutineScope: CoroutineScope
    var canCallRefreshListener = true

    internal val isScopeInitialized: Boolean get() = ::coroutineScope.isInitialized

    fun getRefreshContentState(): State<RefreshContentState> = refreshContentState

    fun createRefreshContentOffsetFlow(): Flow<Float> =
        snapshotFlow { refreshContentOffsetState.value }

    fun getLastRefreshTime(): State<Long?> = lastRefreshTimeState

    fun createLastRefreshTimeFlow(): Flow<Long?> =
        snapshotFlow { lastRefreshTimeState.value }

    fun injectClock(clock: () -> Long) {
        nowMillis = clock
    }

    fun setLastRefreshTime(millis: Long?) {
        lastRefreshTimeState.value = millis
    }

    fun millisSinceLastRefresh(): Long? =
        lastRefreshTimeState.value?.let { (nowMillis() - it).coerceAtLeast(0L) }

    fun getComposePositionState(): State<ComposePosition> = composePositionState

    fun getRefreshContentThreshold(): Float = refreshContentThresholdState.floatValue

    fun getRefreshContentOffset(): Float = refreshContentOffsetState.value

    internal fun offsetHoming() {
        coroutineScope.launch {
            if (abs(refreshContentOffsetState.value) >= refreshContentThresholdState.floatValue) {
                refreshContentState.value = RefreshContentState.Refreshing
                runRefreshListener()
                parkHeaderIfStillRefreshing()
            } else {
                refreshContentOffsetState.animateTo(0f)
                refreshContentState.value = RefreshContentState.Finished
            }
        }
    }

    internal suspend fun runRefreshListener() {
        if (!canCallRefreshListener) {
            finishFromRefresh()
            return
        }
        try {
            onRefreshListener()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            LogCat.e("Refresh listener failed: $e")
            finishFromRefresh()
        }
    }

    private suspend fun finishFromRefresh() {
        refreshContentState.value = RefreshContentState.Finished
        refreshContentOffsetState.animateTo(0f)
    }

    internal suspend fun animateToThreshold() {
        val composePosition = composePositionState.value
        if (composePosition == ComposePosition.Start || composePosition == ComposePosition.Top)
            refreshContentOffsetState.animateTo(refreshContentThresholdState.floatValue)
        else
            refreshContentOffsetState.animateTo(-refreshContentThresholdState.floatValue)
    }

    // Failure recovery already retracted the header; do not park it again.
    internal suspend fun parkHeaderIfStillRefreshing() {
        if (refreshContentState.value == RefreshContentState.Refreshing) {
            animateToThreshold()
        }
    }

    internal fun markRefreshed() {
        lastRefreshTimeState.value = nowMillis()
    }

    internal fun offset(refreshContentOffset: Float, maxOffset: Float = Float.POSITIVE_INFINITY) {
        coroutineScope.launch {
            // Clamp when queued gestures execute, not only when they read the previous frame's offset.
            val requested = refreshContentOffsetState.value + refreshContentOffset
            val targetValue = when (composePositionState.value) {
                ComposePosition.Top, ComposePosition.Start -> requested.coerceIn(0f, maxOffset)
                ComposePosition.Bottom, ComposePosition.End -> requested.coerceIn(-maxOffset, 0f)
            }
            if (refreshContentState.value != RefreshContentState.Refreshing && targetValue != 0f) {
                refreshContentState.value = RefreshContentState.Dragging
            }
            refreshContentOffsetState.snapTo(targetValue)
        }
    }
}

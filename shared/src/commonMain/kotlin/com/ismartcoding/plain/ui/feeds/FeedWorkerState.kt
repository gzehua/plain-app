package com.ismartcoding.plain.ui.feeds

import androidx.compose.runtime.mutableStateMapOf
import com.ismartcoding.plain.models.FeedSyncState
import com.ismartcoding.plain.features.feeds.FeedsClient
import com.ismartcoding.plain.features.feeds.FeedWorkerStatus
import com.ismartcoding.plain.api.graphql.models.FeedSyncUpdate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object FeedWorkerState {
    private val lock = Mutex()
    private val states = mutableStateMapOf<String, FeedSyncState>()

    fun status(id: String): FeedWorkerStatus? = states[id]?.let { FeedWorkerStatus.valueOf(it.status) }

    internal fun revision(id: String): Long? = states[id]?.revision

    suspend fun refresh() = lock.withLock { applyLocked(FeedsClient.syncStates(), replace = true) }
    internal suspend fun apply(update: FeedSyncUpdate) = lock.withLock {
        withContext(Dispatchers.Main) { update.removed.forEach(states::remove) }
        applyLocked(update.states, replace = false)
    }

    private suspend fun applyLocked(values: List<FeedSyncState>, replace: Boolean) = withContext(Dispatchers.Main) {
        if (replace) states.keys.retainAll(values.map { it.feedId }.toSet())
        values.forEach { state ->
            if (!replace && (states[state.feedId]?.revision ?: -1L) > state.revision) return@forEach
            states[state.feedId] = state
        }
    }
}

package com.ismartcoding.plain.ui.feeds

import com.ismartcoding.plain.api.graphql.models.FeedSyncUpdate
import com.ismartcoding.plain.models.FeedSyncState
import com.ismartcoding.plain.features.feeds.FeedWorkerStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class FeedWorkerStateTest {
    @Test
    fun delayed_completion_cannot_replace_a_newer_request_and_removal_is_projected() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val id = "feed-projection-order"
            FeedWorkerState.apply(FeedSyncUpdate(listOf(FeedSyncState(id, "new-task", 20, "QUEUED", "")), emptyList()))
            FeedWorkerState.apply(FeedSyncUpdate(listOf(FeedSyncState(id, "old-task", 19, "COMPLETED", "")), emptyList()))
            assertEquals(FeedWorkerStatus.QUEUED, FeedWorkerState.status(id))
            FeedWorkerState.apply(FeedSyncUpdate(listOf(FeedSyncState(id, "new-task", 21, "RUNNING", "")), emptyList()))
            assertEquals(FeedWorkerStatus.RUNNING, FeedWorkerState.status(id))
            FeedWorkerState.apply(FeedSyncUpdate(emptyList(), listOf(id)))
            assertNull(FeedWorkerState.status(id))
        } finally {
            Dispatchers.resetMain()
        }
    }
}

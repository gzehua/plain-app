package com.ismartcoding.plain.models

import com.ismartcoding.plain.features.feeds.FeedWorkerStatus
import kotlinx.serialization.Serializable

@Serializable
data class FeedSyncState(
    var feedId: String,
    var taskId: String,
    var revision: Long,
    var status: FeedWorkerStatus,
    var error: String,
)

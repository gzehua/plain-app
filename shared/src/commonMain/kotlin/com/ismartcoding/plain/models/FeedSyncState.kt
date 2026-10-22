package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class FeedSyncState(
    var feedId: String,
    var taskId: String,
    var revision: Long,
    var status: String,
    var error: String,
)

package com.ismartcoding.plain.features.feeds

import kotlinx.serialization.Serializable

@Serializable
enum class FeedWorkerStatus {
    QUEUED,
    RUNNING,
    CANCELLED,
    COMPLETED,
    ERROR,
}

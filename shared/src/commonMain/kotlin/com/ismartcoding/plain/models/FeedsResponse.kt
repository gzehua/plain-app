package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class FeedsResponse(
    val feeds: List<Feed>,
    val feedEntryCounts: List<FeedEntryCount>,
)

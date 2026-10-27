package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.models.Feed
import com.ismartcoding.plain.models.FeedEntryCount
import kotlinx.serialization.Serializable

@Serializable
data class FeedsResponse(
    val feeds: List<Feed>,
    val feedEntryCounts: List<FeedEntryCount>,
)

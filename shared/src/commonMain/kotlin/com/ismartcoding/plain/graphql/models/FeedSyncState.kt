package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class FeedSyncState(
    var feedId: String,
    var status: String,
    var error: String,
)

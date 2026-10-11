package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class FeedEntryCount(
    var id: String,
    var count: Int,
)

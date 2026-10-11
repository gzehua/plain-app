package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class MediaBucket(
    var id: String,
    var name: String,
    var itemCount: Int,
    var topItemPaths: List<String>,
)

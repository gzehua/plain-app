package com.ismartcoding.plain.graphql.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Doc(
    var id: String,
    var title: String,
    var path: String,
    var extension: String,
    var size: Long,
    var bucketId: String,
    var createdAt: Instant,
    var updatedAt: Instant,
    var tags: List<Tag>,
)

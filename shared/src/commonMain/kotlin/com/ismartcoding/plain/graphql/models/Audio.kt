package com.ismartcoding.plain.graphql.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Audio(
    var id: String,
    var title: String,
    var artist: String,
    var path: String,
    var durationMs: Long,
    var size: Long,
    var bucketId: String,
    var albumFileId: String,
    var createdAt: Instant,
    var updatedAt: Instant,
    var isFavorite: Boolean,
    var tags: List<Tag>,
)

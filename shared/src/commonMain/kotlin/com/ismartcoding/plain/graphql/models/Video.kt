package com.ismartcoding.plain.graphql.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Video(
    var id: String,
    var title: String,
    var path: String,
    var durationMs: Long,
    var size: Long,
    var bucketId: String,
    var createdAt: Instant,
    var updatedAt: Instant,
    var takenAt: Instant?,
    var isFavorite: Boolean,
)

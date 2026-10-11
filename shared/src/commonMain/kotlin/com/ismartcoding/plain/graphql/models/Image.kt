package com.ismartcoding.plain.graphql.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Image(
    var id: String,
    var title: String,
    var path: String,
    var size: Long,
    var bucketId: String,
    var createdAt: Instant,
    var updatedAt: Instant,
    var takenAt: Instant?,
    var isFavorite: Boolean,
)

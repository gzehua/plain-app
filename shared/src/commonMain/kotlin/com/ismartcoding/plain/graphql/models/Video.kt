package com.ismartcoding.plain.graphql.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Video(
    override var id: String,
    override var title: String,
    override var path: String,
    var durationMs: Long,
    override var size: Long,
    override var bucketId: String,
    override var createdAt: Instant,
    override var updatedAt: Instant,
    var takenAt: Instant?,
    var isFavorite: Boolean,
) : MediaItem

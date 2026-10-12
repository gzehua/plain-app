package com.ismartcoding.plain.graphql.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Image(
    override var id: String,
    override var title: String,
    override var path: String,
    override var size: Long,
    override var bucketId: String,
    override var createdAt: Instant,
    override var updatedAt: Instant,
    var takenAt: Instant?,
    var isFavorite: Boolean,
) : MediaItem

package com.ismartcoding.plain.models

import com.ismartcoding.plain.IMedia
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Audio(
    override var id: String,
    override var title: String,
    var artist: String,
    override var path: String,
    override var durationMs: Long,
    override var size: Long,
    override var bucketId: String,
    var albumFileId: String = "",
    override var createdAt: Instant,
    override var updatedAt: Instant,
    var isFavorite: Boolean = false,
    var albumId: String = "",
) : MediaItem, IMedia

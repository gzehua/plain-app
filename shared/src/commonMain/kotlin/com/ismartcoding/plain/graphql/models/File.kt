package com.ismartcoding.plain.graphql.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class File(
    var mediaId: String?,
    var name: String,
    var path: String,
    var createdAt: Instant?,
    var updatedAt: Instant,
    var size: Long,
    var isDir: Boolean,
    var childCount: Int,
)

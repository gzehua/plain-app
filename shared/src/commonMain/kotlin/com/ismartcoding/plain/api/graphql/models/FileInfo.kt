package com.ismartcoding.plain.api.graphql.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class FileInfo(
    var path: String,
    var updatedAt: Instant,
    var size: Long,
    var data: MediaFileInfo?,
)

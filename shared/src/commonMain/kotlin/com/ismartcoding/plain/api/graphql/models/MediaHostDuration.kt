package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.platform.system.DataType
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class MediaHostDuration(
    var type: DataType,
    var mediaId: String,
    var durationMs: Long,
    var updatedAt: Instant,
    var path: String,
    var sizeBytes: Long,
    var modifiedAt: Instant,
)

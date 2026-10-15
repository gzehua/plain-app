package com.ismartcoding.plain.features

import kotlinx.serialization.Serializable

@Serializable
internal data class MediaDurationReport(
    val mediaType: String,
    val mediaId: String,
    val durationMs: Long,
    val path: String,
    val sizeBytes: Long,
    val modifiedAt: String,
)

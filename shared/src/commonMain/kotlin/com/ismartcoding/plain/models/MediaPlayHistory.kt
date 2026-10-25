package com.ismartcoding.plain.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class MediaPlayHistory(
    val mediaId: String,
    val positionMs: Long,
    val playedAt: Instant,
)

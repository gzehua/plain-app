package com.ismartcoding.plain.graphql.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AudioPlayHistory(
    var mediaId: String,
    var path: String,
    var title: String,
    var artist: String,
    var durationMs: Long,
    var playCount: Int,
    var playedAt: Instant,
)

package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.lib.TimeHelper
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AudioHostHistory(
    var path: String,
    var title: String,
    var artist: String,
    var durationMs: Long,
    var playCount: Long,
    @kotlinx.serialization.Required var playedAt: Instant = TimeHelper.now(),
)

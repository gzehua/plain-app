package com.ismartcoding.plain.models

import com.ismartcoding.plain.lib.TimeHelper
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class VideoPlayProgress(
    var mediaId: String,
    var positionMs: Long,
    var updatedAt: Instant = TimeHelper.now(),
)

package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.lib.TimeHelper
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AudioPlaylistItem(
    var id: String,
    var playlistId: String,
    var audioPath: String,
    var title: String,
    var artist: String,
    var albumId: String? = null,
    var durationMs: Long,
    var sortOrder: Long,
    @kotlinx.serialization.Required var addedAt: Instant = TimeHelper.now(),
)
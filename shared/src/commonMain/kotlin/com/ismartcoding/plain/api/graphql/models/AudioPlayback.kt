package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.features.audio.MediaPlayMode
import kotlinx.serialization.Serializable

@Serializable
data class AudioPlayback(
    var mediaId: String?,
    var path: String?,
    var mode: MediaPlayMode,
    var isPlaying: Boolean,
    var positionMs: Long,
    var revision: Long = 0,
)

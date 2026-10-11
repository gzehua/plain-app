package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.MediaPlayMode
import kotlinx.serialization.Serializable

@Serializable
data class AudioPlayback(
    var currentPath: String?,
    var mode: MediaPlayMode,
    var isPlaying: Boolean,
    var positionMs: Long,
)

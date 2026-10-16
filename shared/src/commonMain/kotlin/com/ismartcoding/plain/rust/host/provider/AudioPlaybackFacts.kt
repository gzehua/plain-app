package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
internal data class AudioPlaybackFacts(
    val isPlaying: Boolean,
    val positionMs: Long,
)

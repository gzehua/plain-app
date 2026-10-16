package com.ismartcoding.plain.features.audio

import kotlinx.serialization.Serializable

@Serializable
internal data class AudioPlaybackReport(val path: String, val revision: Long)

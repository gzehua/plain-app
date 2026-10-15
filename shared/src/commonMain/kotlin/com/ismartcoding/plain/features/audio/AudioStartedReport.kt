package com.ismartcoding.plain.features.audio

import kotlinx.serialization.Serializable

@Serializable
internal data class AudioStartedReport(val path: String, val revision: Long)

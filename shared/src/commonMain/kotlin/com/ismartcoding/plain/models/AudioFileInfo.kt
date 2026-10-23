package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@SerialName("AudioFileInfo")
@Serializable
data class AudioFileInfo(
    var durationMs: Long,
    var location: Location?,
) : MediaFileInfo

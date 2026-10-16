package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@kotlinx.serialization.SerialName("AudioFileInfo")
@Serializable
data class AudioFileInfo(
    var durationMs: Long,
    var location: Location?,
) : MediaFileInfo

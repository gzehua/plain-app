package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@kotlinx.serialization.SerialName("VideoFileInfo")
@Serializable
data class VideoFileInfo(
    var width: Int,
    var height: Int,
    var durationMs: Long,
    var location: Location?,
) : MediaFileInfo

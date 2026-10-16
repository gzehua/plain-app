package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@kotlinx.serialization.SerialName("VideoFileInfo")
@Serializable
data class VideoFileInfo(
    var width: Int,
    var height: Int,
    var durationMs: Long,
    var location: Location?,
    var rotation: Int = 0,
    var displayWidth: Int = 0,
    var displayHeight: Int = 0,
    var bitrate: Long = 0,
    var frameRate: Double = 0.0,
    var title: String = "",
    var artist: String = "",
    var album: String = "",
    var genre: String = "",
    var takenAt: kotlin.time.Instant? = null,
    var writer: String = "",
    var composer: String = "",
) : MediaFileInfo

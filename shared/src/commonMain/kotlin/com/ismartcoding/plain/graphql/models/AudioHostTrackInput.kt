package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class AudioHostTrackInput(
    var path: String,
    var title: String,
    var artist: String,
    var albumId: String,
    var durationMs: Long,
)

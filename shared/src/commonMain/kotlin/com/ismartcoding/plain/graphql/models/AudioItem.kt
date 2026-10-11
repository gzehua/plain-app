package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class AudioItem(
    var title: String,
    var artist: String,
    var albumId: String? = null,
    var path: String,
    var durationMs: Long,
)

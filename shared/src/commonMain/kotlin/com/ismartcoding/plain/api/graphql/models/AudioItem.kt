package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class AudioItem(
    var mediaId: String? = null,
    var title: String,
    var artist: String,
    var path: String,
    var durationMs: Long,
)

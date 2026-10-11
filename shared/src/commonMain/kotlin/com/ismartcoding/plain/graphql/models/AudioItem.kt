package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class AudioItem(
    var title: String,
    var artist: String,
    var path: String,
    var durationMs: Long,
)

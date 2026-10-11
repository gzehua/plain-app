package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class AudioHostSource(
    var source: String,
    var playlistId: String,
    var currentPath: String,
    var currentIndex: Long,
    var sortBy: String,
)

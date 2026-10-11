package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class AudioArtistPlayCount(
    var artist: String,
    var count: Long,
)

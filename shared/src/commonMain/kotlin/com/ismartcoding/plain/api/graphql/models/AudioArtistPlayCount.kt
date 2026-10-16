package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class AudioArtistPlayCount(
    var artist: String,
    var count: Long,
)

package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class AudioPlaylistCovers(val id: String, val albums: List<AudioAlbumCover>)

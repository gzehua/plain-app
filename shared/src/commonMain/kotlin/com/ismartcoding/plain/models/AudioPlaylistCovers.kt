package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class AudioPlaylistCovers(val id: String, val albums: List<AudioAlbumCover>)

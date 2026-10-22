package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class AudioAlbumCover(
    val albumId: String,
    val trackCount: Int,
    val candidatePaths: List<String>,
)

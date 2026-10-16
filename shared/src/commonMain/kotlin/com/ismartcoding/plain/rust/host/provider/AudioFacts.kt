package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
internal data class AudioFacts(
    val id: String,
    val title: String,
    val artist: String,
    val path: String,
    val size: Long,
    val bucketId: String,
    val durationMs: Long,
    val albumArtUri: String,
    val albumId: String,
    val createdAt: String,
    val updatedAt: String,
    val isFavorite: Boolean,
)

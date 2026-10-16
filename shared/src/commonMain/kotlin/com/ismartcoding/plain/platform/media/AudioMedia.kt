package com.ismartcoding.plain.platform.media

import kotlinx.serialization.Serializable

@Serializable
data class AudioMedia(
    val mediaId: String?,
    val path: String,
    val title: String,
    val artist: String,
    val albumId: String,
    val durationMs: Long,
    val aliases: List<String> = emptyList(),
)

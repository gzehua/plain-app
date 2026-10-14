package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.graphql.models.AudioItem
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
) {
    constructor(item: AudioItem, mediaId: String?) : this(mediaId, item.path, item.title, item.artist, "", item.durationMs)
}

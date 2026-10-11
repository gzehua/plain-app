package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.lib.extensions.formatDurationMs
import kotlinx.serialization.Serializable

@Serializable
data class AudioHostTrack(
    var title: String,
    var path: String,
    var artist: String,
    var durationMs: Long,
    var albumId: String = "",
) {
    fun getSubtitle(): String {
        return listOf(artist, durationMs.formatDurationMs()).filter { it.isNotEmpty() }.joinToString(" · ")
    }

    companion object {
        private const val serialVersionUID = -11L
    }
}

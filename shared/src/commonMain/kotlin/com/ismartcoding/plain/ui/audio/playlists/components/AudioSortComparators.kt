package com.ismartcoding.plain.ui.audio.playlists.components

import com.ismartcoding.plain.models.Audio as AudioTrack
import com.ismartcoding.plain.features.files.FileSortBy

/**
 * In-memory [AudioTrack] ordering for lists that are not DB-driven (playlist
 * rows), matching the FileSortBy options the sort dialog offers.
 */
fun List<AudioTrack>.sortedByAudio(sortBy: FileSortBy): List<AudioTrack> {
    val ascending = sortBy == FileSortBy.DATE_ASC || sortBy == FileSortBy.SIZE_ASC || sortBy == FileSortBy.NAME_ASC
    val base = when (sortBy) {
        FileSortBy.DATE_ASC, FileSortBy.DATE_DESC -> sortedBy { it.createdAt }
        FileSortBy.SIZE_ASC, FileSortBy.SIZE_DESC -> sortedBy { it.size }
        else -> sortedBy { it.title.lowercase() }
    }
    return if (ascending) base else base.asReversed()
}

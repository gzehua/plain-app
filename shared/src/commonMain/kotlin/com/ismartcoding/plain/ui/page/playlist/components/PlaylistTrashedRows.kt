package com.ismartcoding.plain.ui.page.playlist.components

import com.ismartcoding.plain.audio.DAudio
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.features.audio.AudioPlaylistManager
import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.graphql.models.AudioItem
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.platform.searchMedia

private const val TRASHED_LOOKUP_LIMIT = 10_000

/** Paths of audio files sitting in MediaStore trash; empty on platforms without trash. */
private suspend fun trashedAudioPaths(): Set<String> = withIO {
    searchMedia(DataType.AUDIO, "trash:true", TRASHED_LOOKUP_LIMIT, 0, FileSortBy.DATE_DESC)
        .filterIsInstance<DAudio>()
        .map { it.path }
        .toSet()
}

/**
 * Drops rows whose audio is trashed and prunes them from the playlist DB, so
 * count, covers and playback agree with the visible list. Trashing cascades at
 * trash time; this heals rows orphaned before that.
 */
internal suspend fun List<AudioItem>.pruneTrashedRows(playlistId: String): List<AudioItem> {
    if (isEmpty()) return this
    val trashed = trashedAudioPaths()
    if (trashed.isEmpty()) return this
    val orphanPaths = filter { it.path in trashed }.map { it.path }
    if (orphanPaths.isEmpty()) return this
    withIO { AudioPlaylistManager.removePlaylistItems(playlistId, orphanPaths) }
    return filterNot { it.path in trashed }
}

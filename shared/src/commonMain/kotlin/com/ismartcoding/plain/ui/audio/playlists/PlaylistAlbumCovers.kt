package com.ismartcoding.plain.ui.audio.playlists

import com.ismartcoding.plain.api.graphql.models.AudioItem
import com.ismartcoding.plain.lib.withIO

/** One album inside a playlist: how many tracks it contributes and where a cover may come from. */
data class PlaylistAlbumCover(
    val albumId: String,
    val trackCount: Int,
    /** Track paths in playlist order; the first with embedded art becomes the tile. */
    val candidatePaths: List<String>,
)

/**
 * Groups playlist tracks by album (MediaStore album id snapshot) and returns
 * the albums carrying the most tracks, up to
 * [MAX_ALBUMS]—[PlaylistMosaicCover] then keeps the first four that actually
 * decode a cover.
 *
 */
internal suspend fun loadPlaylistAlbumCovers(items: List<AudioItem>): List<PlaylistAlbumCover> = withIO {
    val byAlbum = LinkedHashMap<String, MutableList<AudioItem>>()
    val albums = com.ismartcoding.plain.platform.media.audioLibraryResolve(items.mapNotNull { it.mediaId }, emptyList())
        .associate { it.mediaId to it.albumId }
    items.forEach { row ->
        val album = albums[row.mediaId]
        if (!album.isNullOrBlank()) byAlbum.getOrPut(album) { mutableListOf() }.add(row)
    }
    byAlbum.entries
        .sortedByDescending { it.value.size }
        .take(MAX_ALBUMS)
        .map { (album, tracks) ->
            PlaylistAlbumCover(album, tracks.size, tracks.take(CANDIDATES_PER_ALBUM).map { it.path })
        }
}

private const val MAX_ALBUMS = 8
private const val CANDIDATES_PER_ALBUM = 3

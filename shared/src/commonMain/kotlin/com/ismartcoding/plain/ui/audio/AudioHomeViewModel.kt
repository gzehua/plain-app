package com.ismartcoding.plain.ui.audio

import com.ismartcoding.plain.features.audio.AudioQueueClient

import com.ismartcoding.plain.api.graphql.models.Audio as AudioTrack
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ismartcoding.plain.features.audio.AudioHistoryClient
import com.ismartcoding.plain.features.audio.AudioPlaylistsClient
import com.ismartcoding.plain.api.graphql.models.Audio
import com.ismartcoding.plain.api.graphql.models.AudioPlaylist
import com.ismartcoding.plain.ui.audio.playlists.PlaylistAlbumCover
import com.ismartcoding.plain.ui.audio.playlists.loadPlaylistAlbumCovers

data class AudioHomeArtist(val name: String, val itemCount: Int, val playCount: Long, val samplePath: String)

class AudioHomeViewModel : ViewModel() {
    val playlists = mutableStateOf<List<Pair<AudioPlaylist, Int>>>(listOf())

    /** Playlist id -> album covers feeding the home mosaic tiles. */
    val playlistCovers = mutableStateOf<Map<String, List<PlaylistAlbumCover>>>(emptyMap())

    /** 最近 section: play history resolved to library tracks, or recently-added as fallback. */
    val recentItems = mutableStateOf<List<AudioTrack>>(listOf())

    /** All artists, most played first, then by name. */
    val artists = mutableStateOf<List<AudioHomeArtist>>(listOf())

    suspend fun loadAsync(audioVM: AudioViewModel) {
        playlists.value = AudioPlaylistsClient.playlists()
        rebuild(audioVM.itemsFlow.value)
        playlistCovers.value = loadPlaylistCovers()
    }

    private suspend fun loadPlaylistCovers(): Map<String, List<PlaylistAlbumCover>> {
        val covers = mutableMapOf<String, List<PlaylistAlbumCover>>()
        for ((pl, count) in playlists.value) {
            if (count == 0) continue
            val items = AudioPlaylistsClient.playlistItemsPage(pl.id, 0, PLAYLIST_ITEMS_LIMIT)
            if (items.isEmpty()) continue
            covers[pl.id] = loadPlaylistAlbumCovers(items)
        }
        return covers
    }

    suspend fun rebuild(items: List<AudioTrack>) {
        val history = AudioHistoryClient.recentPage(limit = 8, offset = 0)
        recentItems.value = if (history.isNotEmpty()) {
            val byPath = items.associateBy { it.path }
            history.mapNotNull { byPath[it.path] }.ifEmpty {
                items.sortedByDescending { it.createdAt }.take(8)
            }
        } else {
            items.sortedByDescending { it.createdAt }.take(8)
        }
        val playCounts = AudioHistoryClient.artistPlayCounts()
        artists.value = items
            .filter { it.artist.isNotBlank() }
            .groupBy { it.artist }
            .map { (name, list) ->
                AudioHomeArtist(name = name, itemCount = list.size, playCount = playCounts[name] ?: 0, samplePath = list.first().path)
            }
            .sortedWith(compareByDescending<AudioHomeArtist> { it.playCount }.thenBy { it.name })
    }
}

private const val PLAYLIST_ITEMS_LIMIT = 1000

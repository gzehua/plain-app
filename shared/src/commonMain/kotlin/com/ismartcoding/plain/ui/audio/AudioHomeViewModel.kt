package com.ismartcoding.plain.ui.audio

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ismartcoding.plain.models.Audio
import com.ismartcoding.plain.models.AudioArtist
import com.ismartcoding.plain.models.AudioPlaylist
import com.ismartcoding.plain.models.AudioAlbumCover
import com.ismartcoding.plain.features.audio.AudioOverviewClient
import com.ismartcoding.plain.features.audio.AudioPlaylistsClient
import com.ismartcoding.plain.ui.tags.TagsViewModel

class AudioHomeViewModel : ViewModel() {
    val playlists = mutableStateOf<List<Pair<AudioPlaylist, Int>>>(emptyList())
    val playlistCovers = mutableStateOf<Map<String, List<AudioAlbumCover>>>(emptyMap())
    val recentItems = mutableStateOf<List<Audio>>(emptyList())
    val artists = mutableStateOf<List<AudioArtist>>(emptyList())

    suspend fun loadAsync(tagsVM: TagsViewModel) {
        playlists.value = AudioPlaylistsClient.playlists()
        refreshOverview(tagsVM)
        playlistCovers.value = AudioPlaylistsClient.covers(playlists.value.filter { it.second > 0 }.map { it.first.id })
    }

    suspend fun refreshOverview(tagsVM: TagsViewModel) {
        val overview = AudioOverviewClient.read()
        tagsVM.refreshRelations(overview.recentItems.map { it.id }.toSet())
        recentItems.value = overview.recentItems
        artists.value = overview.artists
    }
}

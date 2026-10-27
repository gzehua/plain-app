package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.AudioAlbumCover
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.models.AudioPlaylist
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.rust.graphql.selectionQuery

object AudioPlaylistsClient {
    suspend fun covers(ids: List<String>): Map<String, List<AudioAlbumCover>> {
        if (ids.isEmpty()) return emptyMap()
        return RustGraphqlApi.execute(Query.audioPlaylistCovers(ids))
            .associate { it.id to it.albums }
    }

    suspend fun playlist(id: String): AudioPlaylist? =
        playlists().firstOrNull { it.first.id == id }?.first

    suspend fun playlists(): List<Pair<AudioPlaylist, Int>> =
        RustGraphqlApi.execute(Query.audioPlaylists())
            .map { it to it.itemCount }

    suspend fun createPlaylist(name: String): AudioPlaylist =
        RustGraphqlApi.execute(Mutation.createAudioPlaylist(name))

    suspend fun renamePlaylist(id: String, name: String) {
        RustGraphqlApi.execute(Mutation.updateAudioPlaylist(id, name))
    }

    suspend fun deletePlaylist(id: String) {
        RustGraphqlApi.execute(Mutation.deleteAudioPlaylist(id))
    }

    suspend fun addPlaylistItems(playlistId: String, items: List<AudioItem>) {
        val ids = items.mapNotNull { it.mediaId }
        if (ids.isEmpty()) return
        RustGraphqlApi.execute(Mutation.addAudioPlaylistItems(playlistId, selectionQuery(ids)))
    }

    suspend fun removePlaylistItem(playlistId: String, mediaId: String) =
        removePlaylistItems(playlistId, listOf(mediaId))

    suspend fun removePlaylistItems(playlistId: String, mediaIds: Collection<String>) {
        if (mediaIds.isNotEmpty()) {
            RustGraphqlApi.execute(Mutation.removeAudioPlaylistItem(playlistId, selectionQuery(mediaIds)))
        }
    }

    suspend fun playlistItemsPage(playlistId: String, offset: Int, limit: Int): List<AudioItem> =
        playlistItemsPageFiltered(playlistId, "", offset, limit)

    suspend fun playlistItemsPageFiltered(
        playlistId: String,
        text: String,
        offset: Int,
        limit: Int,
    ): List<AudioItem> =
        RustGraphqlApi.execute(Query.audioPlaylistItems(playlistId, offset, limit, text))
}

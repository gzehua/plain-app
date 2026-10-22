package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.models.AudioAlbumCover
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.models.AudioPlaylist
import com.ismartcoding.plain.models.AudioPlaylistCovers
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.rust.request.gqlIds
import com.ismartcoding.plain.rust.request.selectionQuery
import kotlinx.serialization.json.*

object AudioPlaylistsClient {
    suspend fun covers(ids: List<String>): Map<String, List<AudioAlbumCover>> {
        if (ids.isEmpty()) return emptyMap()
        return PlainRust.mainQuery("audioPlaylistCovers(ids: ${gqlIds(ids)}) { id albums { albumId trackCount candidatePaths } }")
            .getValue("audioPlaylistCovers").jsonArray
            .map { JsonHelper.jsonDecodeFromElement<AudioPlaylistCovers>(it) }
            .associate { it.id to it.albums }
    }

    suspend fun playlist(id: String): AudioPlaylist? = playlists().firstOrNull { it.first.id == id }?.first
    suspend fun playlists(): List<Pair<AudioPlaylist, Int>> = PlainRust.mainQuery("audioPlaylists { $AUDIO_PLAYLIST_FIELDS }")
        .getValue("audioPlaylists").let { JsonHelper.jsonDecodeFromElement<List<AudioPlaylist>>(it) }.map { it to it.itemCount }
    suspend fun createPlaylist(name: String): AudioPlaylist = PlainRust.mainMutate("createAudioPlaylist(name: ${gql(name)}) { $AUDIO_PLAYLIST_FIELDS }")
        .getValue("createAudioPlaylist").audioPlaylist()
    suspend fun renamePlaylist(id: String, name: String) { PlainRust.mainMutate("updateAudioPlaylist(id: ${gql(id)}, name: ${gql(name)}) { id }") }
    suspend fun deletePlaylist(id: String) { PlainRust.mainMutate("deleteAudioPlaylist(id: ${gql(id)})") }
    suspend fun addPlaylistItems(playlistId: String, items: List<AudioItem>) {
        val ids = items.mapNotNull { it.mediaId }
        if (ids.isEmpty()) return
        PlainRust.mainMutate("addAudioPlaylistItems(id: ${gql(playlistId)}, query: ${gql(selectionQuery(ids))})")
    }
    suspend fun removePlaylistItem(playlistId: String, mediaId: String) = removePlaylistItems(playlistId,listOf(mediaId))
    suspend fun removePlaylistItems(playlistId: String, mediaIds: Collection<String>) { if (mediaIds.isNotEmpty()) PlainRust.mainMutate("removeAudioPlaylistItem(id: ${gql(playlistId)}, query: ${gql(selectionQuery(mediaIds))}) { affectedCount }") }
    suspend fun playlistItemsPage(playlistId: String, offset: Int, limit: Int): List<AudioItem> = playlistItemsPageFiltered(playlistId,"",offset,limit)
    suspend fun playlistItemsPageFiltered(playlistId: String, text: String, offset: Int, limit: Int): List<AudioItem> =
        PlainRust.mainQuery("audioPlaylistItems(id: ${gql(playlistId)}, offset: $offset, limit: $limit, query: ${gql(text)}) { $AUDIO_ITEM_FIELDS }")
            .getValue("audioPlaylistItems").jsonArray.map { it.audioItem() }
}

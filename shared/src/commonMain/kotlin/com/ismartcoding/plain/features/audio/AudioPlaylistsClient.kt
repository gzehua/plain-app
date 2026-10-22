package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.models.AudioAlbumCover
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.models.AudioPlaylist
import com.ismartcoding.plain.models.AudioPlaylistCovers
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.graphql.gql
import com.ismartcoding.plain.rust.graphql.gqlIds
import com.ismartcoding.plain.rust.graphql.selectionQuery
import kotlinx.serialization.json.*

object AudioPlaylistsClient {
    suspend fun covers(ids: List<String>): Map<String, List<AudioAlbumCover>> {
        if (ids.isEmpty()) return emptyMap()
        return RustGraphqlApi.query("audioPlaylistCovers(ids: ${gqlIds(ids)}) { id albums { albumId trackCount candidatePaths } }")
            .getValue("audioPlaylistCovers").jsonArray
            .map { JsonHelper.jsonDecodeFromElement<AudioPlaylistCovers>(it) }
            .associate { it.id to it.albums }
    }

    suspend fun playlist(id: String): AudioPlaylist? = playlists().firstOrNull { it.first.id == id }?.first
    suspend fun playlists(): List<Pair<AudioPlaylist, Int>> = RustGraphqlApi.query("audioPlaylists { $AUDIO_PLAYLIST_FIELDS }")
        .getValue("audioPlaylists").let { JsonHelper.jsonDecodeFromElement<List<AudioPlaylist>>(it) }.map { it to it.itemCount }
    suspend fun createPlaylist(name: String): AudioPlaylist = RustGraphqlApi.mutate("createAudioPlaylist(name: ${gql(name)}) { $AUDIO_PLAYLIST_FIELDS }")
        .getValue("createAudioPlaylist").audioPlaylist()
    suspend fun renamePlaylist(id: String, name: String) { RustGraphqlApi.mutate("updateAudioPlaylist(id: ${gql(id)}, name: ${gql(name)}) { id }") }
    suspend fun deletePlaylist(id: String) { RustGraphqlApi.mutate("deleteAudioPlaylist(id: ${gql(id)})") }
    suspend fun addPlaylistItems(playlistId: String, items: List<AudioItem>) {
        val ids = items.mapNotNull { it.mediaId }
        if (ids.isEmpty()) return
        RustGraphqlApi.mutate("addAudioPlaylistItems(id: ${gql(playlistId)}, query: ${gql(selectionQuery(ids))})")
    }
    suspend fun removePlaylistItem(playlistId: String, mediaId: String) = removePlaylistItems(playlistId,listOf(mediaId))
    suspend fun removePlaylistItems(playlistId: String, mediaIds: Collection<String>) { if (mediaIds.isNotEmpty()) RustGraphqlApi.mutate("removeAudioPlaylistItem(id: ${gql(playlistId)}, query: ${gql(selectionQuery(mediaIds))}) { affectedCount }") }
    suspend fun playlistItemsPage(playlistId: String, offset: Int, limit: Int): List<AudioItem> = playlistItemsPageFiltered(playlistId,"",offset,limit)
    suspend fun playlistItemsPageFiltered(playlistId: String, text: String, offset: Int, limit: Int): List<AudioItem> =
        RustGraphqlApi.query("audioPlaylistItems(id: ${gql(playlistId)}, offset: $offset, limit: $limit, query: ${gql(text)}) { $AUDIO_ITEM_FIELDS }")
            .getValue("audioPlaylistItems").jsonArray.map { it.audioItem() }
}

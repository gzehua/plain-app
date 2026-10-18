package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.api.graphql.models.AudioItem
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.api.graphql.models.AudioAlbumCover
import com.ismartcoding.plain.api.graphql.models.AudioPlaylistCovers
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.api.graphql.models.AudioPlaylist
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.features.feeds.*
import com.ismartcoding.plain.ui.feeds.*
import com.ismartcoding.plain.features.files.*
import com.ismartcoding.plain.ui.files.*
import com.ismartcoding.plain.features.notes.*
import com.ismartcoding.plain.ui.notes.*
import com.ismartcoding.plain.features.tags.*
import com.ismartcoding.plain.ui.tags.*
import com.ismartcoding.plain.rust.request.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.request.*
import kotlinx.serialization.json.*

object AudioPlaylistsClient {
    suspend fun covers(ids: List<String>): Map<String, List<AudioAlbumCover>> {
        if (ids.isEmpty()) return emptyMap()
        return RustContentApi.query("audioPlaylistCovers(ids: ${gqlIds(ids)}) { id albums { albumId trackCount candidatePaths } }")
            .getValue("audioPlaylistCovers").jsonArray
            .map { JsonHelper.jsonDecodeFromElement<AudioPlaylistCovers>(it) }
            .associate { it.id to it.albums }
    }

    suspend fun playlist(id: String): AudioPlaylist? = playlists().firstOrNull { it.first.id == id }?.first
    suspend fun playlists(): List<Pair<AudioPlaylist, Int>> = RustContentApi.query("audioPlaylists { $AUDIO_PLAYLIST_FIELDS }")
        .getValue("audioPlaylists").jsonArray.map { it.audioPlaylist() to it.jsonObject.getValue("itemCount").jsonPrimitive.int }
    suspend fun createPlaylist(name: String): AudioPlaylist = RustContentApi.mutate("createAudioPlaylist(name: ${gql(name)}) { $AUDIO_PLAYLIST_FIELDS }")
        .getValue("createAudioPlaylist").audioPlaylist()
    suspend fun renamePlaylist(id: String, name: String) { RustContentApi.mutate("updateAudioPlaylist(id: ${gql(id)}, name: ${gql(name)}) { id }") }
    suspend fun deletePlaylist(id: String) { RustContentApi.mutate("deleteAudioPlaylist(id: ${gql(id)})") }
    suspend fun addPlaylistItems(playlistId: String, items: List<AudioItem>) {
        val ids = items.mapNotNull { it.mediaId }
        if (ids.isEmpty()) return
        RustContentApi.mutate("addAudioPlaylistItems(id: ${gql(playlistId)}, query: ${gql("ids:" + ids.joinToString(","))})")
    }
    suspend fun removePlaylistItem(playlistId: String, mediaId: String) = removePlaylistItems(playlistId,listOf(mediaId))
    suspend fun removePlaylistItems(playlistId: String, mediaIds: Collection<String>) { if (mediaIds.isNotEmpty()) RustContentApi.mutate("removeAudioPlaylistItem(id: ${gql(playlistId)}, query: ${gql("ids:" + mediaIds.joinToString(","))}) { affectedCount }") }
    suspend fun playlistItemsPage(playlistId: String, offset: Int, limit: Int): List<AudioItem> = playlistItemsPageFiltered(playlistId,"",offset,limit)
    suspend fun playlistItemsPageFiltered(playlistId: String, text: String, offset: Int, limit: Int): List<AudioItem> =
        RustContentApi.query("audioPlaylistItems(id: ${gql(playlistId)}, offset: $offset, limit: $limit, query: ${gql(text)}) { $AUDIO_ITEM_FIELDS }")
            .getValue("audioPlaylistItems").jsonArray.map { it.audioItem() }
    suspend fun playlistItemCount(playlistId: String): Int = RustContentApi.query("audioPlaylistItemCount(id: ${gql(playlistId)}, query: \"\")").getValue("audioPlaylistItemCount").jsonPrimitive.int
}

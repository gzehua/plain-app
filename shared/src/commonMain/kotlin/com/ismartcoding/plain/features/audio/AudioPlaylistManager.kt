package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.graphql.models.AudioItem
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.graphql.models.AudioPlaylist
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.content.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.request.*
import kotlinx.serialization.json.*

object AudioPlaylistManager {
    suspend fun playlist(id: String): AudioPlaylist? = playlists().firstOrNull { it.first.id == id }?.first
    suspend fun playlists(): List<Pair<AudioPlaylist, Int>> = RustContentApi.query("audioPlaylists { $AUDIO_PLAYLIST_FIELDS }")
        .getValue("audioPlaylists").jsonArray.map { it.audioPlaylist() to it.jsonObject.getValue("itemCount").jsonPrimitive.int }
    suspend fun createPlaylist(name: String): AudioPlaylist = RustContentApi.mutate("createAudioPlaylist(name: ${gql(name)}) { $AUDIO_PLAYLIST_FIELDS }")
        .getValue("createAudioPlaylist").audioPlaylist()
    suspend fun renamePlaylist(id: String, name: String) { RustContentApi.mutate("updateAudioPlaylist(id: ${gql(id)}, name: ${gql(name)}) { id }") }
    suspend fun deletePlaylist(id: String) { RustContentApi.mutate("deleteAudioPlaylist(id: ${gql(id)})") }
    suspend fun addPlaylistItems(playlistId: String, items: List<AudioItem>): Int =
        PlainRust.privateMutate("audioHostAddPlaylistItems(id: ${gql(playlistId)}, mediaIds: ${gqlIds(items.mapNotNull { it.mediaId })})").getValue("audioHostAddPlaylistItems").jsonPrimitive.int
    suspend fun removePlaylistItem(playlistId: String, mediaId: String) = removePlaylistItems(playlistId,listOf(mediaId))
    suspend fun removePlaylistItems(playlistId: String, mediaIds: Collection<String>) { if (mediaIds.isNotEmpty()) RustContentApi.mutate("removeAudioPlaylistItem(id: ${gql(playlistId)}, query: ${gql("ids:" + mediaIds.joinToString(","))}) { affectedCount }") }
    suspend fun playlistItemsPage(playlistId: String, offset: Int, limit: Int): List<AudioItem> = playlistItemsPageFiltered(playlistId,"",offset,limit)
    suspend fun playlistItemsPageFiltered(playlistId: String, text: String, offset: Int, limit: Int): List<AudioItem> =
        RustContentApi.query("audioPlaylistItems(id: ${gql(playlistId)}, offset: $offset, limit: $limit, query: ${gql(text)}) { $AUDIO_ITEM_FIELDS }")
            .getValue("audioPlaylistItems").jsonArray.map { it.audioItem() }
    suspend fun playlistItemCount(playlistId: String): Int = RustContentApi.query("audioPlaylistItemCount(id: ${gql(playlistId)}, query: \"\")").getValue("audioPlaylistItemCount").jsonPrimitive.int
}

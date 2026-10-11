package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.graphql.models.AudioItem
import com.ismartcoding.plain.graphql.models.AudioPlaylist
import com.ismartcoding.plain.graphql.models.AudioPlaylistItem
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.content.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.request.*
import kotlinx.serialization.json.*

object AudioPlaylistManager {
    suspend fun playlist(id: String): AudioPlaylist? = RustContentApi.query("audioPlaylist(id: ${gql(id)}) { $AUDIO_PLAYLIST_FIELDS }")
        .getValue("audioPlaylist").takeUnless { it is JsonNull }?.audioPlaylist()
    suspend fun playlists(): List<Pair<AudioPlaylist, Int>> = RustContentApi.query("audioPlaylists { $AUDIO_PLAYLIST_FIELDS }")
        .getValue("audioPlaylists").jsonArray.map { it.audioPlaylist() to it.jsonObject.getValue("itemCount").jsonPrimitive.int }
    suspend fun createPlaylist(name: String): AudioPlaylist = RustContentApi.mutate("createAudioPlaylist(name: ${gql(name)}) { $AUDIO_PLAYLIST_FIELDS }")
        .getValue("createAudioPlaylist").audioPlaylist()
    suspend fun renamePlaylist(id: String, name: String) { RustContentApi.mutate("updateAudioPlaylist(id: ${gql(id)}, name: ${gql(name)})") }
    suspend fun deletePlaylist(id: String) { RustContentApi.mutate("deleteAudioPlaylist(id: ${gql(id)})") }
    suspend fun addPlaylistItems(playlistId: String, items: List<AudioItem>): Int =
        RustContentApi.mutate("audioHostAddPlaylistItems(id: ${gql(playlistId)}, items: ${items.audioInput()})").getValue("audioHostAddPlaylistItems").jsonPrimitive.int
    suspend fun removePlaylistItem(playlistId: String, path: String) = removePlaylistItems(playlistId,listOf(path))
    suspend fun removePlaylistItems(playlistId: String, paths: Collection<String>) { RustContentApi.mutate("audioHostRemovePlaylistItems(id: ${gql(playlistId)}, paths: ${gqlIds(paths)})") }
    suspend fun playlistItemsPage(playlistId: String, offset: Int, limit: Int): List<AudioPlaylistItem> = playlistItemsPageFiltered(playlistId,"",offset,limit)
    suspend fun playlistItemsPageFiltered(playlistId: String, text: String, offset: Int, limit: Int): List<AudioPlaylistItem> =
        RustContentApi.query("audioPlaylistItems(id: ${gql(playlistId)}, offset: $offset, limit: $limit, query: ${gql(text)}) { $AUDIO_ITEM_FIELDS }")
            .getValue("audioPlaylistItems").jsonArray.map { it.audioItem() }
    suspend fun playlistItemCount(playlistId: String): Int = RustContentApi.query("audioPlaylistItemCount(id: ${gql(playlistId)})").getValue("audioPlaylistItemCount").jsonPrimitive.int
}

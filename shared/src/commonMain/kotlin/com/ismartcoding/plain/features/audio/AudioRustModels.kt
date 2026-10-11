package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.db.*
import com.ismartcoding.plain.graphql.models.AudioHostHistory
import com.ismartcoding.plain.graphql.models.AudioHostPlaylist
import com.ismartcoding.plain.graphql.models.AudioHostPlaylistItem
import com.ismartcoding.plain.graphql.models.AudioHostTrack
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.content.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.request.*
import kotlinx.serialization.json.*

internal const val AUDIO_TRACK_FIELDS = "path title artist albumId durationMs"
internal const val AUDIO_PLAYLIST_FIELDS = "id name itemCount createdAt updatedAt"
internal const val AUDIO_ITEM_FIELDS = "id playlistId audioPath title artist albumId durationMs sortOrder addedAt"
internal const val AUDIO_HISTORY_FIELDS = "path title artist durationMs playCount playedAt"
internal fun JsonElement.audioTrack(): AudioHostTrack = JsonHelper.jsonDecodeFromElement<AudioHostTrack>(this)
internal fun List<AudioHostTrack>.audioInput(): String = joinToString(",", "[", "]") { r ->
    "{path:${gql(r.path)},title:${gql(r.title)},artist:${gql(r.artist)},albumId:${gql(r.albumId)},durationMs:${r.durationMs}}"
}
internal fun JsonElement.audioPlaylist(): AudioHostPlaylist = JsonHelper.jsonDecodeFromElement<AudioHostPlaylist>(this)
internal fun JsonElement.audioItem(): AudioHostPlaylistItem = JsonHelper.jsonDecodeFromElement<AudioHostPlaylistItem>(this)
internal fun JsonElement.audioHistory(): AudioHostHistory = JsonHelper.jsonDecodeFromElement<AudioHostHistory>(this)

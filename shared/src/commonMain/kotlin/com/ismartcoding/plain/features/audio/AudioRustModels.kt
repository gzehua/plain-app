package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.db.*
import com.ismartcoding.plain.graphql.models.AudioItem
import com.ismartcoding.plain.graphql.models.AudioPlayHistory
import com.ismartcoding.plain.graphql.models.AudioPlaylist
import com.ismartcoding.plain.graphql.models.AudioPlaylistItem
import com.ismartcoding.plain.graphql.models.AudioHostTrackInput
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
internal fun JsonElement.audioTrack(): AudioItem = JsonHelper.jsonDecodeFromElement<AudioItem>(this)
internal fun List<AudioItem>.audioInput(): String = joinToString(",", "[", "]") { r ->
    "{path:${gql(r.path)},title:${gql(r.title)},artist:${gql(r.artist)},albumId:${gql(r.albumId ?: "")},durationMs:${r.durationMs}}"
}
internal fun JsonElement.audioPlaylist(): AudioPlaylist = JsonHelper.jsonDecodeFromElement<AudioPlaylist>(this)
internal fun JsonElement.audioItem(): AudioPlaylistItem = JsonHelper.jsonDecodeFromElement<AudioPlaylistItem>(this)
internal fun JsonElement.audioHistory(): AudioPlayHistory = JsonHelper.jsonDecodeFromElement<AudioPlayHistory>(this)

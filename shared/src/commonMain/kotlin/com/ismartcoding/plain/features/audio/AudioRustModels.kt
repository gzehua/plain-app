package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.db.*
import com.ismartcoding.plain.api.graphql.models.AudioItem
import com.ismartcoding.plain.api.graphql.models.AudioPlayHistory
import com.ismartcoding.plain.api.graphql.models.AudioPlaylist
import com.ismartcoding.plain.lib.JsonHelper
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

internal const val AUDIO_TRACK_FIELDS = "mediaId path title artist durationMs"
internal const val AUDIO_PLAYLIST_FIELDS = "id name itemCount createdAt updatedAt"
internal const val AUDIO_ITEM_FIELDS = AUDIO_TRACK_FIELDS
internal const val AUDIO_HISTORY_FIELDS = "mediaId path title artist durationMs playCount playedAt"
internal fun JsonElement.audioTrack(): AudioItem = JsonHelper.jsonDecodeFromElement<AudioItem>(this)
internal fun List<AudioItem>.audioInput(): String = joinToString(",", "[", "]") { r ->
    "{mediaId:${r.mediaId?.let(::gql) ?: "null"},path:${gql(r.path)},title:${gql(r.title)},artist:${gql(r.artist)},albumId:${gql("")},durationMs:${r.durationMs}}"
}
internal fun JsonElement.audioPlaylist(): AudioPlaylist = JsonHelper.jsonDecodeFromElement<AudioPlaylist>(this)
internal fun JsonElement.audioItem(): AudioItem = JsonHelper.jsonDecodeFromElement<AudioItem>(this)
internal fun JsonElement.audioHistory(): AudioPlayHistory = JsonHelper.jsonDecodeFromElement<AudioPlayHistory>(this)

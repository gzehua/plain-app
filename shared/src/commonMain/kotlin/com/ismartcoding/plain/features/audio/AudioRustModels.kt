package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.api.graphql.models.AudioItem
import com.ismartcoding.plain.api.graphql.models.AudioPlaylist
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal const val AUDIO_TRACK_FIELDS = "mediaId path title artist durationMs"
internal const val AUDIO_PLAYLIST_FIELDS = "id name itemCount createdAt updatedAt"
internal const val AUDIO_ITEM_FIELDS = AUDIO_TRACK_FIELDS
internal fun JsonElement.audioTrack(): AudioItem = JsonHelper.jsonDecodeFromElement<AudioItem>(this)
internal fun JsonElement.audioPlaylist(): AudioPlaylist = JsonHelper.jsonDecodeFromElement<AudioPlaylist>(this)
internal fun JsonElement.audioItem(): AudioItem = JsonHelper.jsonDecodeFromElement<AudioItem>(this)

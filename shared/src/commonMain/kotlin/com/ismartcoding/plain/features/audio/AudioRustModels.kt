package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.models.AudioPlaylist
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal fun JsonElement.audioTrack(): AudioItem = JsonHelper.jsonDecodeFromElement<AudioItem>(this)
internal fun JsonElement.audioPlaylist(): AudioPlaylist = JsonHelper.jsonDecodeFromElement<AudioPlaylist>(this)
internal fun JsonElement.audioItem(): AudioItem = JsonHelper.jsonDecodeFromElement<AudioItem>(this)

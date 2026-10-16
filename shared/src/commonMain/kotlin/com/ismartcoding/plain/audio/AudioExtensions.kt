package com.ismartcoding.plain.audio

import com.ismartcoding.plain.graphql.models.Audio
import com.ismartcoding.plain.graphql.models.AudioItem
import com.ismartcoding.plain.lib.extensions.formatDurationMs

fun Audio.getSubtitle(): String =
    listOf(artist, durationMs.formatDurationMs()).filter { it.isNotEmpty() }.joinToString(" · ")

fun Audio.toPlaylistAudio(): AudioItem =
    AudioItem(mediaId = id, title = title, artist = artist, path = path, durationMs = durationMs)

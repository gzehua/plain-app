package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.db.DAudioQueueItem
import com.ismartcoding.plain.graphql.models.AudioHostPlaylistItem
import com.ismartcoding.plain.graphql.models.AudioHostTrack

fun AudioHostPlaylistItem.toPlaylistAudio(): AudioHostTrack =
    AudioHostTrack(title = title, path = audioPath, artist = artist, durationMs = durationMs, albumId = albumId)

fun DAudioQueueItem.toPlaylistAudio(): AudioHostTrack =
    AudioHostTrack(title = title, path = path, artist = artist, durationMs = durationMs)

fun AudioHostTrack.toQueueItem(sortOrder: Int): DAudioQueueItem =
    DAudioQueueItem(path = path, sortOrder = sortOrder, title = title, artist = artist, durationMs = durationMs)

package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.db.DAudioQueueItem
import com.ismartcoding.plain.graphql.models.AudioItem
import com.ismartcoding.plain.graphql.models.AudioPlaylistItem

fun AudioPlaylistItem.toPlaylistAudio(): AudioItem =
    AudioItem(title = title, path = audioPath, artist = artist, durationMs = durationMs, albumId = albumId)

fun DAudioQueueItem.toPlaylistAudio(): AudioItem =
    AudioItem(title = title, path = path, artist = artist, durationMs = durationMs)

fun AudioItem.toQueueItem(sortOrder: Int): DAudioQueueItem =
    DAudioQueueItem(path = path, sortOrder = sortOrder, title = title, artist = artist, durationMs = durationMs)

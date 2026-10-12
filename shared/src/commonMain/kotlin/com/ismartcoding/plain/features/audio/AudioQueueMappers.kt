package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.db.DAudioQueueItem
import com.ismartcoding.plain.graphql.models.AudioItem

fun DAudioQueueItem.toPlaylistAudio(): AudioItem =
    AudioItem(title = title, path = path, artist = artist, durationMs = durationMs)

fun AudioItem.toQueueItem(sortOrder: Int): DAudioQueueItem =
    DAudioQueueItem(path = path, sortOrder = sortOrder, title = title, artist = artist, durationMs = durationMs)

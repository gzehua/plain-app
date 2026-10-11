package com.ismartcoding.plain.db

import com.ismartcoding.plain.graphql.models.AudioHostSource

/** Where the queue draws its tracks from. Stored as readable TEXT (enum name). */
enum class AudioPlaySource {
    /** No source — only manually queued items can play. */
    NONE,

    /** A user playlist ([AudioHostSource.playlistId]). */
    PLAYLIST,

    /** The whole audio library, ordered by [AudioHostSource.sortBy]. */
    LIBRARY,
}

/**
 * Single-row (id = 1) playback state: what is playing and where it comes from.
 * The queue itself is never materialized — next/previous are resolved from the
 * source on demand, so a 10k-track library costs the same as one track.
 */

/** Manually queued tracks ("play next" / "add to queue"), ordered by [DAudioQueueItem.sortOrder]. */
data class DAudioQueueItem(
    val path: String,
    var sortOrder: Int,
    var title: String,
    var artist: String,
    var durationMs: Long,
)

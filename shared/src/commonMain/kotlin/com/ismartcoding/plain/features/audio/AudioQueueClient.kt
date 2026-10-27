package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.models.AudioPlayback
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.rust.graphql.selectionQuery

object AudioQueueClient {
    suspend fun playback(): AudioPlayback =
        RustGraphqlApi.execute(Query.audioPlayback())

    suspend fun resolveNext(isNext: Boolean, shuffle: Boolean = false): AudioItem? {
        val current = playback()
        val action = if (isNext) AudioSelectionAction.NEXT else AudioSelectionAction.PREVIOUS
        val result = RustGraphqlApi.execute(
            Mutation.advanceAudioSelection(action, current.path.orEmpty(), current.revision, shuffle),
        )
        return result?.track
    }

    suspend fun clearQueue() {
        RustGraphqlApi.execute(Mutation.clearAudioQueue())
    }

    suspend fun enqueue(items: List<AudioItem>) {
        val ids = items.mapNotNull { it.mediaId }.filter { it.isNotEmpty() }
        if (ids.isNotEmpty()) add(selectionQuery(ids.toSet()))
    }

    suspend fun add(query: String) {
        RustGraphqlApi.execute(Mutation.addAudioToQueue(query))
    }

    suspend fun removeQueued(mediaId: String) {
        RustGraphqlApi.execute(Mutation.removeAudioFromQueue(mediaId))
    }

    suspend fun reorderQueued(mediaIds: List<String>) {
        RustGraphqlApi.execute(Mutation.reorderAudioQueue(mediaIds))
    }

    suspend fun queuedMediaIds(): Set<String> =
        RustGraphqlApi.execute(Query.audioQueueIds(Int.MAX_VALUE))
            .mapNotNull { it.mediaId }
            .toSet()

    suspend fun queueTotal(): Int =
        RustGraphqlApi.execute(Query.audioQueueItemCount())

    suspend fun queuePage(offset: Int, limit: Int): List<AudioItem> =
        queuePageFiltered("", offset, limit)

    suspend fun queuePageFiltered(query: String, offset: Int, limit: Int): List<AudioItem> =
        RustGraphqlApi.execute(Query.audioQueueItems(offset, limit, query))
}

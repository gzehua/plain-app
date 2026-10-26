package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import kotlinx.serialization.Serializable
import com.ismartcoding.plain.models.AudioPlayback
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.rust.graphql.*
import kotlinx.serialization.json.*

object AudioQueueClient {
    suspend fun playback(): AudioPlayback = JsonHelper.jsonDecodeFromElement(
        RustGraphqlApi.graphql(Query.audioPlayback()).getValue("audioPlayback"))

    suspend fun resolveNext(isNext: Boolean, shuffle: Boolean = false): AudioItem? {
        val current = playback()
        val action = if (isNext) "NEXT" else "PREVIOUS"
        val result = RustGraphqlApi.graphql(Mutation.advanceAudioSelection(action, current.path.orEmpty(), current.revision, shuffle))
            .getValue("advanceAudioSelection")
        if (result is JsonNull) return null
        val track = result.jsonObject.getValue("track")
        return if (track is JsonNull) null else JsonHelper.jsonDecodeFromElement(track)
    }

    suspend fun clearQueue() { RustGraphqlApi.graphql(Mutation.clearAudioQueue()) }

    suspend fun enqueue(items: List<AudioItem>) {
        val ids = items.mapNotNull { it.mediaId }.filter { it.isNotEmpty() }
        if (ids.isNotEmpty()) add(selectionQuery(ids.toSet()))
    }

    suspend fun add(query: String) { RustGraphqlApi.graphql(Mutation.addAudioToQueue(query)) }
    suspend fun removeQueued(mediaId: String) { RustGraphqlApi.graphql(Mutation.removeAudioFromQueue(mediaId)) }
    suspend fun reorderQueued(mediaIds: List<String>) { RustGraphqlApi.graphql(Mutation.reorderAudioQueue(mediaIds)) }
    suspend fun queuedMediaIds(): Set<String> = RustGraphqlApi.graphql(
        Query.audioQueueIds(Int.MAX_VALUE),
    ).getValue("audioQueueItems").let { JsonHelper.jsonDecodeFromElement<List<QueuedMediaId>>(it) }.mapNotNull { it.mediaId }.toSet()

    suspend fun queueTotal(): Int = RustGraphqlApi.graphql(Query.audioQueueItemCount()).getValue("audioQueueItemCount").jsonPrimitive.int
    suspend fun queuePage(offset: Int, limit: Int): List<AudioItem> = queuePageFiltered("",offset,limit)
    suspend fun queuePageFiltered(query: String, offset: Int, limit: Int): List<AudioItem> =
        RustGraphqlApi.graphql(Query.audioQueueItems(offset, limit, query))
            .getValue("audioQueueItems").jsonArray.map { it.audioTrack() }
}

@Serializable
private data class QueuedMediaId(val mediaId: String?)

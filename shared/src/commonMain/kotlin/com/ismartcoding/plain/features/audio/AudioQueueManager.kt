package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.graphql.models.AudioPlayback
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.graphql.models.AudioItem
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.*
import com.ismartcoding.plain.rust.request.RustContentApi
import kotlinx.serialization.json.*

object AudioQueueManager {
    suspend fun playback(): AudioPlayback = JsonHelper.jsonDecodeFromElement(
        RustContentApi.query("audioPlayback { mediaId path mode isPlaying positionMs }").getValue("audioPlayback"))

    suspend fun clearQueue() { RustContentApi.mutate("clearAudioQueue") }

    suspend fun enqueue(items: List<AudioItem>) {
        val ids = items.mapNotNull { it.mediaId }.filter { it.isNotEmpty() }
        if (ids.isNotEmpty()) PlainRust.mainMutate("enqueueAudios(mediaIds: ${gqlIds(ids)})")
    }

    suspend fun add(query: String) { RustContentApi.mutate("addAudioToQueue(query: ${gql(query)})") }
    suspend fun removeQueued(mediaId: String) { RustContentApi.mutate("removeAudioFromQueue(mediaId: ${gql(mediaId)})") }
    suspend fun reorderQueued(mediaIds: List<String>) { RustContentApi.mutate("reorderAudioQueue(mediaIds: ${gqlIds(mediaIds)})") }
    suspend fun queuedMediaIds(): Set<String> = PlainRust.mainQuery("audioQueuedMediaIds").getValue("audioQueuedMediaIds").jsonArray.map { it.jsonPrimitive.content }.toSet()
    suspend fun resolveNext(isNext: Boolean, shuffle: Boolean): AudioItem? =
        PlainRust.mainMutate("selectNextAudio(isNext: $isNext, shuffle: $shuffle) { $AUDIO_TRACK_FIELDS }")
            .getValue("selectNextAudio").takeUnless { it is JsonNull }?.audioTrack()

    suspend fun onStarted(audio: AudioItem, revision: Long) {
        PlainRust.publish(com.ismartcoding.plain.events.HostEvent(
            com.ismartcoding.plain.events.EventType.AUDIO_STARTED_REPORTED,
            JsonHelper.jsonEncode(AudioStartedReport(audio.path, revision)),
        ))
    }

    suspend fun setCurrent(mediaId: String?) { PlainRust.mainMutate("selectAudio(mediaId: ${mediaId?.let(::gql) ?: "null"})") }
    suspend fun queueTotal(): Int = RustContentApi.query("audioQueueItemCount").getValue("audioQueueItemCount").jsonPrimitive.int
    suspend fun queuePage(offset: Int, limit: Int): List<AudioItem> = queuePageFiltered("",offset,limit)
    suspend fun queuePageFiltered(query: String, offset: Int, limit: Int): List<AudioItem> =
        RustContentApi.query("audioQueueItems(offset: $offset, limit: $limit, query: ${gql(query)}) { $AUDIO_TRACK_FIELDS }")
            .getValue("audioQueueItems").jsonArray.map { it.audioTrack() }
}

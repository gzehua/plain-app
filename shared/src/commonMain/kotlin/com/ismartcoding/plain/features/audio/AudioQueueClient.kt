package com.ismartcoding.plain.features.audio

import kotlinx.serialization.Serializable

import com.ismartcoding.plain.models.AudioPlayback
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.*
import com.ismartcoding.plain.rust.request.RustApi
import kotlinx.serialization.json.*

object AudioQueueClient {
    suspend fun playback(): AudioPlayback = JsonHelper.jsonDecodeFromElement(
        RustApi.query("audioPlayback { mediaId path mode isPlaying positionMs revision }").getValue("audioPlayback"))

    suspend fun resolveNext(isNext: Boolean, shuffle: Boolean = false): AudioItem? {
        val current = playback()
        val action = if (isNext) "NEXT" else "PREVIOUS"
        val result = PlainRust.mainMutate("advanceAudioSelection(action: $action, expectedPath: ${gql(current.path.orEmpty())}, expectedRevision: ${current.revision}, shuffle: $shuffle) { track { $AUDIO_TRACK_FIELDS } }")
            .getValue("advanceAudioSelection")
        if (result is JsonNull) return null
        val track = result.jsonObject.getValue("track")
        return if (track is JsonNull) null else JsonHelper.jsonDecodeFromElement(track)
    }

    suspend fun clearQueue() { RustApi.mutate("clearAudioQueue") }

    suspend fun enqueue(items: List<AudioItem>) {
        val ids = items.mapNotNull { it.mediaId }.filter { it.isNotEmpty() }
        if (ids.isNotEmpty()) add(selectionQuery(ids.toSet()))
    }

    suspend fun add(query: String) { RustApi.mutate("addAudioToQueue(query: ${gql(query)})") }
    suspend fun removeQueued(mediaId: String) { RustApi.mutate("removeAudioFromQueue(mediaId: ${gql(mediaId)})") }
    suspend fun reorderQueued(mediaIds: List<String>) { RustApi.mutate("reorderAudioQueue(mediaIds: ${gqlIds(mediaIds)})") }
    suspend fun queuedMediaIds(): Set<String> = PlainRust.mainQuery(
        "audioQueueItems(query: \"\", offset: 0, limit: ${Int.MAX_VALUE}) { mediaId }",
    ).getValue("audioQueueItems").let { JsonHelper.jsonDecodeFromElement<List<QueuedMediaId>>(it) }.mapNotNull { it.mediaId }.toSet()

    suspend fun onStarted(audio: AudioItem, revision: Long) {
        PlainRust.publish(com.ismartcoding.plain.app.events.HostEvent(
            com.ismartcoding.plain.app.events.EventType.AUDIO_STARTED_REPORTED,
            JsonHelper.jsonEncode(AudioPlaybackReport(audio.path, revision)),
        ))
    }

    suspend fun onCompleted(path: String, revision: Long) {
        PlainRust.publish(com.ismartcoding.plain.app.events.HostEvent(
            com.ismartcoding.plain.app.events.EventType.AUDIO_COMPLETED_REPORTED,
            JsonHelper.jsonEncode(AudioPlaybackReport(path, revision)),
        ))
    }

    suspend fun queueTotal(): Int = RustApi.query("audioQueueItemCount").getValue("audioQueueItemCount").jsonPrimitive.int
    suspend fun queuePage(offset: Int, limit: Int): List<AudioItem> = queuePageFiltered("",offset,limit)
    suspend fun queuePageFiltered(query: String, offset: Int, limit: Int): List<AudioItem> =
        RustApi.query("audioQueueItems(offset: $offset, limit: $limit, query: ${gql(query)}) { $AUDIO_TRACK_FIELDS }")
            .getValue("audioQueueItems").jsonArray.map { it.audioTrack() }
}

@Serializable
private data class QueuedMediaId(val mediaId: String?)

package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.db.AudioPlaySource
import com.ismartcoding.plain.graphql.models.AudioHostSource
import com.ismartcoding.plain.graphql.models.AudioHostTrack
import com.ismartcoding.plain.preferences.UserPrefs
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.content.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.request.*
import kotlinx.serialization.json.*

object AudioQueueManager {
    suspend fun source(): AudioHostSource = RustContentApi.query("audioSource { source playlistId currentPath currentIndex sortBy }")
        .getValue("audioSource").let { com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement<AudioHostSource>(it) }

    suspend fun activePlaylistId(): String? = source().let { if (it.source == AudioPlaySource.PLAYLIST.name) it.playlistId else null }

    suspend fun setPlaylistSource(playlistId: String, startPath: String?): AudioHostTrack? =
        selected("audioHostSetPlaylistSource(id: ${gql(playlistId)}, startPath: ${startPath?.let(::gql) ?: "null"})", "audioHostSetPlaylistSource")

    suspend fun setLibrarySource(startPath: String?, shuffle: Boolean): AudioHostTrack? =
        selected("audioHostSetLibrarySource(startPath: ${startPath?.let(::gql) ?: "null"}, shuffle: $shuffle, sortBy: ${gql(UserPrefs.audioSortBy.value.name)})", "audioHostSetLibrarySource")

    suspend fun clearQueue() { RustContentApi.mutate("audioHostClear") }

    suspend fun enqueue(items: List<AudioHostTrack>, playNext: Boolean = false) {
        RustContentApi.mutate("audioHostEnqueue(items: ${items.audioInput()}, playNext: $playNext)")
    }

    suspend fun removeQueued(path: String) { RustContentApi.mutate("audioHostRemoveQueued(path: ${gql(path)})") }
    suspend fun moveQueued(from: Int, to: Int) { RustContentApi.mutate("audioHostMoveQueued(from: $from, to: $to)") }
    suspend fun reorderQueued(paths: List<String>) { RustContentApi.mutate("audioHostReorder(paths: ${gqlIds(paths)})") }
    suspend fun queuedPaths(): Set<String> = RustContentApi.query("audioQueuedPaths").getValue("audioQueuedPaths").jsonArray.map { it.jsonPrimitive.content }.toSet()
    suspend fun removePaths(paths: Collection<String>) { RustContentApi.mutate("audioHostRemovePaths(paths: ${gqlIds(paths)})") }
    suspend fun resolveNext(isNext: Boolean, shuffle: Boolean): AudioHostTrack? =
        selected("audioHostResolveNext(isNext: $isNext, shuffle: $shuffle)", "audioHostResolveNext")

    suspend fun onPlaying(path: String, title: String, artist: String, durationMs: Long) {
        RustContentApi.mutate("audioHostOnPlaying(path: ${gql(path)}, title: ${gql(title)}, artist: ${gql(artist)}, durationMs: $durationMs)")
    }

    suspend fun onStarted(audio: AudioHostTrack, revision: Long) {
        val track = listOf(audio).audioInput().removePrefix("[").removeSuffix("]")
        RustContentApi.mutate("audioReportStarted(track: $track, revision: $revision)")
    }

    suspend fun setCurrent(path: String) { RustContentApi.mutate("audioHostSetCurrent(path: ${gql(path)})") }
    suspend fun queueTotal(): Int = RustContentApi.query("audioHostQueueCount").getValue("audioHostQueueCount").jsonPrimitive.int
    suspend fun queuePage(offset: Int, limit: Int): List<AudioHostTrack> = queuePageFiltered("",offset,limit)
    suspend fun queuePageFiltered(text: String, offset: Int, limit: Int): List<AudioHostTrack> =
        RustContentApi.query("audioHostQueueItems(offset: $offset, limit: $limit, query: ${gql(text)}) { $AUDIO_TRACK_FIELDS }")
            .getValue("audioHostQueueItems").jsonArray.map { it.audioTrack() }

    private suspend fun selected(selection: String, field: String): AudioHostTrack? =
        RustContentApi.mutate("$selection { $AUDIO_TRACK_FIELDS }").getValue(field).takeUnless { it is JsonNull }?.audioTrack()
}

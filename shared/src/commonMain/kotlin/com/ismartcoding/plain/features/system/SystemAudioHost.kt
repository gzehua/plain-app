package com.ismartcoding.plain.features.system

import com.ismartcoding.plain.graphql.models.AudioItem
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object SystemAudioHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemAudioPlaylistTracks" -> JsonHelper.jsonEncodeToElement(
            params.getValue("paths").jsonArray.map { value ->
                val track = com.ismartcoding.plain.platform.playlistAudioFromPath(
                    value.jsonPrimitive.content)
                playlistTrackFacts(track)
        })
        "systemAudioSearchTracks" -> JsonHelper.jsonEncodeToElement(
            com.ismartcoding.plain.platform.searchMedia(
                com.ismartcoding.plain.enums.DataType.AUDIO,
                params.getValue("query").jsonPrimitive.content,
                params.getValue("limit").jsonPrimitive.int,
                params.getValue("offset").jsonPrimitive.int,
                com.ismartcoding.plain.features.file.FileSortBy.valueOf(
                    params.getValue("sortBy").jsonPrimitive.content),
            ).filterIsInstance<com.ismartcoding.plain.audio.DAudio>()
            .map { playlistTrackFacts(com.ismartcoding.plain.graphql.models.AudioItem(
                mediaId = it.id, title = it.title, path = it.path, artist = it.artist,
                durationMs = it.durationMs)) })
        "systemAudioLyrics" -> JsonHelper.jsonEncodeToElement(
            com.ismartcoding.plain.platform.getAudioLyrics(
                params.getValue("path").jsonPrimitive.content))
        "systemAudioPlaybackPermission" -> JsonHelper.jsonEncodeToElement(
            com.ismartcoding.plain.platform.ensureNotificationPermissionAsync())
        "systemAudioPlaybackState" -> JsonHelper.jsonEncodeToElement(AudioPlaybackFacts(
            isPlaying = com.ismartcoding.plain.platform.audioIsPlayingFlow().value,
            positionMs = com.ismartcoding.plain.platform.audioPlayerProgressAsync(),
        ))
        else -> error("Unsupported provider operation")
    }

    private fun playlistTrackFacts(track: com.ismartcoding.plain.graphql.models.AudioItem): PlaylistTrackFacts =
        PlaylistTrackFacts(
            mediaId = track.mediaId,
            title = track.title,
            artist = track.artist,
            path = track.path,
            durationMs = track.durationMs,
        )
}

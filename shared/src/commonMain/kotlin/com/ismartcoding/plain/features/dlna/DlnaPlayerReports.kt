package com.ismartcoding.plain.features.dlna

import com.ismartcoding.plain.rust.event.RustEventHandler

import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HostEvent
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable

internal object DlnaPlayerReports {
    private fun report(event: HostEvent) { coIO { RustEventHandler.publish(event) } }
    fun reportPosition(mediaUri: String, positionMs: Long, durationMs: Long) = report(position(mediaUri, positionMs, durationMs))
    fun reportSeekConsumed(mediaUri: String, positionMs: Long) = report(seekConsumed(mediaUri, positionMs))
    fun reportPlaybackState(mediaUri: String, state: DlnaPlaybackState) = report(playback(mediaUri, state))
    fun reportStopped(mediaUri: String) = report(stopped(mediaUri))

    fun position(mediaUri: String, positionMs: Long, durationMs: Long) =
        HostEvent(EventType.DLNA_POSITION_REPORTED, JsonHelper.jsonEncode(Position(mediaUri, positionMs, durationMs)))

    fun playback(mediaUri: String, state: DlnaPlaybackState) =
        HostEvent(EventType.DLNA_PLAYBACK_REPORTED, JsonHelper.jsonEncode(Playback(mediaUri, state)))

    fun seekConsumed(mediaUri: String, positionMs: Long) =
        HostEvent(EventType.DLNA_SEEK_CONSUMED_REPORTED, JsonHelper.jsonEncode(Seek(mediaUri, positionMs)))

    fun stopped(mediaUri: String) =
        HostEvent(EventType.DLNA_STOPPED_REPORTED, JsonHelper.jsonEncode(Stopped(mediaUri)))
}

@Serializable
private data class Position(val mediaUri: String, val positionMs: Long, val durationMs: Long)

@Serializable
private data class Playback(val mediaUri: String, val state: DlnaPlaybackState)

@Serializable
private data class Seek(val mediaUri: String, val positionMs: Long)

@Serializable
private data class Stopped(val mediaUri: String)

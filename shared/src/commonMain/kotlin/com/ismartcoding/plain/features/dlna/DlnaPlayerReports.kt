package com.ismartcoding.plain.features.dlna

import com.ismartcoding.plain.rust.native.RustHostReports
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable

internal object DlnaPlayerReports {
    fun reportPosition(mediaUri: String, positionMs: Long, durationMs: Long) {
        coIO {
            RustHostReports.dlnaPosition(JsonHelper.jsonEncode(Position(mediaUri, positionMs, durationMs)))
        }
    }
    fun reportSeekConsumed(mediaUri: String, positionMs: Long) {
        coIO {
            RustHostReports.dlnaSeekConsumed(JsonHelper.jsonEncode(Seek(mediaUri, positionMs)))
        }
    }
    fun reportPlaybackState(mediaUri: String, state: DlnaPlaybackState) {
        coIO {
            RustHostReports.dlnaPlayback(JsonHelper.jsonEncode(Playback(mediaUri, state)))
        }
    }
    fun reportStopped(mediaUri: String) {
        coIO {
            RustHostReports.dlnaStopped(JsonHelper.jsonEncode(Stopped(mediaUri)))
        }
    }
}

@Serializable
internal data class Position(val mediaUri: String, val positionMs: Long, val durationMs: Long)

@Serializable
internal data class Playback(val mediaUri: String, val state: DlnaPlaybackState)

@Serializable
internal data class Seek(val mediaUri: String, val positionMs: Long)

@Serializable
internal data class Stopped(val mediaUri: String)

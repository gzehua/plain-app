package com.ismartcoding.plain.rust.host.audio

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.native.RustHostReports
import kotlinx.serialization.Serializable

internal sealed interface AudioHostReport {
    @Serializable
    data class Started(val path: String, val revision: Long) : AudioHostReport

    @Serializable
    data class Completed(val path: String, val revision: Long) : AudioHostReport

    @Serializable
    data class Progress(val path: String, val positionMs: Long, val revision: Long) : AudioHostReport

    suspend fun sendToRust() {
        when (this) {
            is Started -> RustHostReports.audioStarted(JsonHelper.jsonEncode(this))
            is Completed -> RustHostReports.audioCompleted(JsonHelper.jsonEncode(this))
            is Progress -> RustHostReports.audioProgress(JsonHelper.jsonEncode(this))
        }
    }
}

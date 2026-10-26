package com.ismartcoding.plain.rust.host.audio

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.sendHostTextToRust
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
            is Started -> sendHostTextToRust("AUDIO_STARTED_REPORTED", JsonHelper.jsonEncode(this))
            is Completed -> sendHostTextToRust("AUDIO_COMPLETED_REPORTED", JsonHelper.jsonEncode(this))
            is Progress -> sendHostTextToRust("AUDIO_PROGRESS_REPORTED", JsonHelper.jsonEncode(this))
        }
    }
}

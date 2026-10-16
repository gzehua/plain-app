package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object SystemAudioHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemAudioPlaybackState" -> JsonHelper.jsonEncodeToElement(AudioPlaybackFacts(
            isPlaying = com.ismartcoding.plain.platform.media.audioIsPlayingFlow().value,
            positionMs = com.ismartcoding.plain.platform.media.audioPlayerProgressAsync(),
        ))
        else -> error("Unsupported provider operation")
    }

}

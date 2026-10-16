package com.ismartcoding.plain.features.system

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object SystemAudioHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemAudioPlaybackState" -> JsonHelper.jsonEncodeToElement(AudioPlaybackFacts(
            isPlaying = com.ismartcoding.plain.platform.audioIsPlayingFlow().value,
            positionMs = com.ismartcoding.plain.platform.audioPlayerProgressAsync(),
        ))
        else -> error("Unsupported provider operation")
    }

}

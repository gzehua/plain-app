package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.platform.permissions.isGranted
import kotlinx.serialization.json.*

internal object SystemScreenMirrorHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemScreenMirrorState" -> {
            val codec = com.ismartcoding.plain.platform.media.getScreenMirrorVideoCodec()
            JsonHelper.jsonEncodeToElement(ScreenMirrorStateFacts(
                running = com.ismartcoding.plain.platform.media.isScreenMirrorRunning(),
                controlEnabled = com.ismartcoding.plain.platform.media.isScreenMirrorControlEnabled(),
                codec = codec?.let { value ->
                    ScreenMirrorCodecFacts(
                        annexB = value.annexB,
                        keyFrame = value.keyFrame,
                    )
                },
            ))
        }
        "systemStartScreenMirror" -> {
            com.ismartcoding.plain.platform.media.applyScreenMirrorQualityPreference()
            sendEvent(
                com.ismartcoding.plain.app.events.HStartScreenMirrorEvent(
                    params.getValue("audio").jsonPrimitive.boolean
                )
            )
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemStopScreenMirror" -> {
            com.ismartcoding.plain.platform.media.stopScreenMirror()
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemRequestScreenMirrorAudio" -> {
            val granted = com.ismartcoding.plain.platform.permissions.Permission.RECORD_AUDIO.isGranted()
            if (!granted) {
                sendEvent(
                    com.ismartcoding.plain.app.events.HRequestScreenMirrorAudioEvent()
                )
            }
            JsonHelper.jsonEncodeToElement(granted)
        }
        "systemRequestScreenMirrorKeyFrame" -> {
            com.ismartcoding.plain.platform.media.requestScreenMirrorKeyFrame()
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemUpdateScreenMirrorQuality" -> {
            com.ismartcoding.plain.preferences.Prefs.refresh()
            val mode = com.ismartcoding.plain.features.screenmirror.ScreenMirrorMode.valueOf(params.getValue("mode").jsonPrimitive.content)
            com.ismartcoding.plain.platform.media.onScreenMirrorQualityChanged(mode)
            JsonHelper.jsonEncodeToElement(true)
        }
        else -> error("Unsupported provider operation")
    }
}

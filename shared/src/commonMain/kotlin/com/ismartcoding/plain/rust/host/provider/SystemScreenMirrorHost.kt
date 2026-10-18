package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

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
            val request = JsonHelper.jsonDecodeFromElement<StartScreenMirrorRequest>(params)
            com.ismartcoding.plain.platform.media.applyScreenMirrorQualityPreference()
            sendEvent(
                com.ismartcoding.plain.app.events.HStartScreenMirrorEvent(
                    request.audio
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
            val request = JsonHelper.jsonDecodeFromElement<ScreenMirrorQualityRequest>(params)
            com.ismartcoding.plain.preferences.Prefs.refresh()
            val mode = com.ismartcoding.plain.features.screenmirror.ScreenMirrorMode.valueOf(request.mode)
            com.ismartcoding.plain.platform.media.onScreenMirrorQualityChanged(mode)
            JsonHelper.jsonEncodeToElement(true)
        }
        else -> error("Unsupported provider operation")
    }
}

@Serializable
private data class StartScreenMirrorRequest(val audio: Boolean)

@Serializable
private data class ScreenMirrorQualityRequest(val mode: String)

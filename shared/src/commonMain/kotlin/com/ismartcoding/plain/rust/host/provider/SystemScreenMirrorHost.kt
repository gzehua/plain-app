package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.platform.permissions.isGranted
import com.ismartcoding.plain.platform.media.ScreenMirrorControlsFacts
import com.ismartcoding.plain.platform.media.dispatchScreenMirrorControl
import com.ismartcoding.plain.platform.media.resetScreenMirrorTouchStream
import kotlinx.serialization.json.*

internal object SystemScreenMirrorHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemScreenMirrorControls" -> {
            JsonHelper.jsonDecodeFromElement<ScreenMirrorControlsFacts>(params).inputs.forEach { dispatchScreenMirrorControl(it) }
            JsonNull
        }
        "systemScreenMirrorResetTouch" -> {
            resetScreenMirrorTouchStream()
            JsonNull
        }
        "systemScreenMirrorState" -> {
            val codec = com.ismartcoding.plain.platform.media.getScreenMirrorVideoCodec()
            JsonHelper.jsonEncodeToElement(ScreenMirrorStateFacts(
                running = com.ismartcoding.plain.platform.media.isScreenMirrorRunning(),
                controlEnabled = com.ismartcoding.plain.platform.media.isScreenMirrorControlEnabled(),
                codec = codec,
            ))
        }
        "systemStartScreenMirror" -> {
            val request = JsonHelper.jsonDecodeFromElement<StartScreenMirrorRequest>(params)
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
            com.ismartcoding.plain.preferences.PrefStore.refresh()
            JsonHelper.jsonEncodeToElement(true)
        }
        else -> error("Unsupported provider operation")
    }
}

@Serializable
private data class StartScreenMirrorRequest(val audio: Boolean)


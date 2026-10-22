package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.app.events.HRequestScreenMirrorAudioEvent
import com.ismartcoding.plain.app.events.HStartScreenMirrorEvent
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.platform.media.ScreenMirrorControlsFacts
import com.ismartcoding.plain.platform.media.dispatchScreenMirrorControl
import com.ismartcoding.plain.platform.media.getScreenMirrorVideoCodec
import com.ismartcoding.plain.platform.media.isScreenMirrorControlEnabled
import com.ismartcoding.plain.platform.media.isScreenMirrorRunning
import com.ismartcoding.plain.platform.media.requestScreenMirrorKeyFrame
import com.ismartcoding.plain.platform.media.resetScreenMirrorTouchStream
import com.ismartcoding.plain.platform.media.stopScreenMirror
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.permissions.isGranted
import com.ismartcoding.plain.rust.host.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

internal object SystemScreenMirrorHost {
    internal suspend fun handle(method: String, params: JsonObject): HostResult = when (method) {
        "systemScreenMirrorControls" -> {
            JsonHelper.jsonDecodeFromElement<ScreenMirrorControlsFacts>(params).inputs.forEach { dispatchScreenMirrorControl(it) }
            hostResult()
        }
        "systemScreenMirrorResetTouch" -> {
            resetScreenMirrorTouchStream()
            hostResult()
        }
        "systemScreenMirrorState" -> {
            val codec = getScreenMirrorVideoCodec()
            hostResult(ScreenMirrorStateFacts(
                running = isScreenMirrorRunning(),
                controlEnabled = isScreenMirrorControlEnabled(),
                codec = codec,
            ))
        }
        "systemStartScreenMirror" -> {
            val request = JsonHelper.jsonDecodeFromElement<StartScreenMirrorRequest>(params)
            sendEvent(
                HStartScreenMirrorEvent(
                    request.audio
                )
            )
            hostResult(true)
        }
        "systemStopScreenMirror" -> {
            stopScreenMirror()
            hostResult(true)
        }
        "systemRequestScreenMirrorAudio" -> {
            val granted = Permission.RECORD_AUDIO.isGranted()
            if (!granted) {
                sendEvent(
                    HRequestScreenMirrorAudioEvent()
                )
            }
            hostResult(granted)
        }
        "systemRequestScreenMirrorKeyFrame" -> {
            requestScreenMirrorKeyFrame()
            hostResult(true)
        }
        else -> error("Unsupported provider operation")
    }
}

@Serializable
private data class StartScreenMirrorRequest(val audio: Boolean)

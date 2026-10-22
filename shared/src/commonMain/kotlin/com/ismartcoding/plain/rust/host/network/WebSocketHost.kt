package com.ismartcoding.plain.rust.host.network

import com.ismartcoding.plain.platform.media.ScreenMirrorControlsFacts
import com.ismartcoding.plain.platform.media.dispatchScreenMirrorControl
import com.ismartcoding.plain.platform.media.resetScreenMirrorTouchStream

import com.ismartcoding.plain.features.chat.peer.PeerManager
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object WebSocketHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement {
        when (method) {
            "systemScreenMirrorControls" -> JsonHelper.jsonDecodeFromElement<ScreenMirrorControlsFacts>(params).inputs.forEach { dispatchScreenMirrorControl(it) }
            "systemScreenMirrorResetTouch" -> resetScreenMirrorTouchStream()
            else -> error("Unsupported WebSocket platform operation")
        }
        return JsonNull
    }
}

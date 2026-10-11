package com.ismartcoding.plain.chat.peer

import com.ismartcoding.plain.platform.startAwareIfNeeded
import com.ismartcoding.plain.platform.subscribeAwareForPeer
import kotlinx.serialization.json.*

object PeerAwareHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "peerStartAware" -> {
            val peer = RustPeerStore.decode(params.getValue("peer"))
            val started = startAwareIfNeeded()
            if (started) subscribeAwareForPeer(peer)
            JsonPrimitive(started)
        }
        else -> error("Unknown peer Host operation: $method")
    }
}

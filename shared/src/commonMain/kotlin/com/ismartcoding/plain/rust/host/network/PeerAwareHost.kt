package com.ismartcoding.plain.rust.host.network

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.network.AwareConfig
import com.ismartcoding.plain.platform.network.AwarePeer
import com.ismartcoding.plain.platform.network.startAwareIfNeeded
import com.ismartcoding.plain.platform.network.subscribeAwareForPeer
import com.ismartcoding.plain.platform.network.unsubscribeAwareForPeer
import kotlinx.serialization.json.*

object PeerAwareHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "peerStartAware" -> {
            val peer = JsonHelper.jsonDecodeFromElement<AwarePeer>(params.getValue("peer"))
            val config = JsonHelper.jsonDecodeFromElement<AwareConfig>(params.getValue("config"))
            PeerSocketHost.disconnectPeer(peer.id)
            val started = startAwareIfNeeded()
            if (started) subscribeAwareForPeer(peer, config) else unsubscribeAwareForPeer(peer.id)
            JsonPrimitive(started)
        }
        "peerStopAware" -> {
            val id = params.getValue("id").jsonPrimitive.content
            PeerSocketHost.disconnectPeer(id)
            unsubscribeAwareForPeer(id)
            JsonPrimitive(true)
        }
        else -> error("Unknown peer Host operation: $method")
    }
}

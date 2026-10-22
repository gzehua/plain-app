package com.ismartcoding.plain.rust.host.network

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.network.AwareConfig
import com.ismartcoding.plain.platform.network.AwarePeer
import com.ismartcoding.plain.platform.network.startAwareIfNeeded
import com.ismartcoding.plain.platform.network.subscribeAwareForPeer
import com.ismartcoding.plain.platform.network.unsubscribeAwareForPeer
import com.ismartcoding.plain.rust.host.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

object PeerAwareHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "peerStartAware" -> {
            val request = JsonHelper.jsonDecodeFromElement<StartAwareRequest>(params)
            val peer = request.peer
            val config = request.config
            PeerSocketHost.disconnectPeer(peer.id)
            val started = startAwareIfNeeded()
            if (started) subscribeAwareForPeer(peer, config) else unsubscribeAwareForPeer(peer.id)
            JsonPrimitive(started)
        }
        "peerStopAware" -> {
            val id = JsonHelper.jsonDecodeFromElement<IdRequest>(params).id
            PeerSocketHost.disconnectPeer(id)
            unsubscribeAwareForPeer(id)
            JsonPrimitive(true)
        }
        else -> error("Unknown peer Host operation: $method")
    }
}

@Serializable
private data class StartAwareRequest(val peer: AwarePeer, val config: AwareConfig)

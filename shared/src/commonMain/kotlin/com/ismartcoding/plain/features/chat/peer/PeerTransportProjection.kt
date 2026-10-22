package com.ismartcoding.plain.features.chat.peer

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable

import com.ismartcoding.plain.ui.chat.ChatViewModel
import com.ismartcoding.plain.features.chat.peer.transport.PeerTransportType
import kotlinx.serialization.json.*

object PeerTransportProjection {
    fun apply(active: JsonArray) {
        ChatViewModel.currentTransportMap.value = JsonHelper.jsonDecodeFromElement<List<ActiveTransport>>(active).associate { row ->
            row.peerId to PeerTransportType.valueOf(row.transport)
        }
    }
}

@Serializable
private data class ActiveTransport(val peerId: String, val transport: String)

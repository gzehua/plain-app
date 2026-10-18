package com.ismartcoding.plain.features.chat.peer

import com.ismartcoding.plain.ui.chat.peer.PeerCacher
import com.ismartcoding.plain.features.chat.peer.transport.PeerTransportType
import kotlinx.serialization.json.*

object PeerTransportProjection {
    fun apply(active: JsonArray) {
        PeerCacher.currentTransportMap.value = active.associate { value ->
            val row = value.jsonObject
            row.getValue("peerId").jsonPrimitive.content to PeerTransportType.valueOf(row.getValue("transport").jsonPrimitive.content)
        }
    }
}

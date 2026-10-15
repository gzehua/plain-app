package com.ismartcoding.plain.chat.peer

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.chat.peer.transport.PeerTransportType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*

object PeerTransportProjection {
    private val lock = Mutex()

    suspend fun refresh() = lock.withLock {
        val active = PlainRust.mainQuery("activePeerTransfers { peerId transport }").getValue("activePeerTransfers").jsonArray
        PeerCacher.currentTransportMap.value = active.associate { value ->
            val row = value.jsonObject
            row.getValue("peerId").jsonPrimitive.content to PeerTransportType.valueOf(row.getValue("transport").jsonPrimitive.content)
        }
    }
}

package com.ismartcoding.plain.features.chat.peer.transport

import com.ismartcoding.plain.ui.chat.peer.PeerCacher

import com.ismartcoding.plain.platform.network.ble.BleUuids
import com.ismartcoding.plain.platform.network.bleTransport
import com.ismartcoding.plain.platform.network.isBleReady
import com.ismartcoding.plain.platform.network.isWifiAwareSupported
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.*

object PeerTransportPrewarmHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "peerTransportPrewarmCapabilities" -> buildJsonObject {
            put("bleReady", isBleReady()); put("awareSupported", isWifiAwareSupported)
        }
        "peerTransportPrewarmScan" -> {
            val device = withTimeoutOrNull(params.getValue("timeoutMs").jsonPrimitive.long) {
                bleTransport().createScanner().scan(BleUuids.SERVICE_UUID).firstOrNull {
                    it.id == params.getValue("shortId").jsonPrimitive.content
                }
            }
            device?.let { buildJsonObject {
                put("shortId", it.id); put("awareSupported", it.awareSupported); put("awareRunning", it.awareRunning)
            } } ?: JsonNull
        }
        else -> error("Unknown peer prewarm operation")
    }
}

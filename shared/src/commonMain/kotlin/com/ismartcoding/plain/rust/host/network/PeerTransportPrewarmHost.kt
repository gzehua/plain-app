package com.ismartcoding.plain.rust.host.network

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.network.ble.BleUuids
import com.ismartcoding.plain.platform.network.bleTransport
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

object PeerTransportPrewarmHost {
    @Serializable
    private data class ScanResult(val shortId: String, val awareSupported: Boolean, val awareRunning: Boolean)

    suspend fun scan(params: JsonObject): JsonElement {
        val request = JsonHelper.jsonDecodeFromElement<PrewarmScanRequest>(params)
        val device = withTimeoutOrNull(request.timeoutMs) {
            bleTransport().createScanner().scan(BleUuids.SERVICE_UUID).firstOrNull {
                it.id == request.shortId
            }
        }
        return device?.let {
            JsonHelper.jsonEncodeToElement(ScanResult(it.id, it.awareSupported, it.awareRunning))
        } ?: JsonNull
    }
}

@Serializable
private data class PrewarmScanRequest(val shortId: String, val timeoutMs: Long)

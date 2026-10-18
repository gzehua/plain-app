package com.ismartcoding.plain.rust.host.network

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.network.ble.BleUuids
import com.ismartcoding.plain.platform.network.bleTransport
import com.ismartcoding.plain.platform.network.isBleReady
import com.ismartcoding.plain.platform.network.isWifiAwareSupported
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

object PeerTransportPrewarmHost {
    @Serializable
    private data class Capabilities(val bleReady: Boolean, val awareSupported: Boolean)

    @Serializable
    private data class ScanResult(val shortId: String, val awareSupported: Boolean, val awareRunning: Boolean)

    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "peerTransportPrewarmCapabilities" -> JsonHelper.jsonEncodeToElement(
            Capabilities(isBleReady(), isWifiAwareSupported)
        )
        "peerTransportPrewarmScan" -> {
            val request = JsonHelper.jsonDecodeFromElement<PrewarmScanRequest>(params)
            val device = withTimeoutOrNull(request.timeoutMs) {
                bleTransport().createScanner().scan(BleUuids.SERVICE_UUID).firstOrNull {
                    it.id == request.shortId
                }
            }
            device?.let {
                JsonHelper.jsonEncodeToElement(ScanResult(it.id, it.awareSupported, it.awareRunning))
            } ?: JsonNull
        }
        else -> error("Unknown peer prewarm operation")
    }
}

@Serializable
private data class PrewarmScanRequest(val shortId: String, val timeoutMs: Long)

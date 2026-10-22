package com.ismartcoding.plain.rust.host.network

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.network.ble.BleUuids
import com.ismartcoding.plain.platform.network.bleTransport
import com.ismartcoding.plain.rust.host.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

object PeerTransportPrewarmHost {
    @Serializable
    private data class ScanResult(val shortId: String, val awareSupported: Boolean, val awareRunning: Boolean)

    internal suspend fun scan(params: JsonObject): HostResult {
        val request = JsonHelper.jsonDecodeFromElement<PrewarmScanRequest>(params)
        val device = withTimeoutOrNull(request.timeoutMs) {
            bleTransport().createScanner().scan(BleUuids.SERVICE_UUID).firstOrNull {
                it.id == request.shortId
            }
        }
        return device?.let {
            hostResult(ScanResult(it.id, it.awareSupported, it.awareRunning))
        } ?: hostResult()
    }
}

@Serializable
private data class PrewarmScanRequest(val shortId: String, val timeoutMs: Long)

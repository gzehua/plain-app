package com.ismartcoding.plain.rust.host.network

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.network.ble.BleUuids
import com.ismartcoding.plain.platform.network.ble.PairingTransport
import com.ismartcoding.plain.rust.host.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

object PairingNotificationHost {
    internal fun handle(params: JsonObject): HostResult {
        val request = JsonHelper.jsonDecodeFromElement<PairingNotificationRequest>(params)
        return hostResult(PairingTransport.sendNotification(request.address, BleUuids.NEARBY_CHAR_UUID, request.body))
    }
}

@Serializable
private data class PairingNotificationRequest(val address: String, val body: String)

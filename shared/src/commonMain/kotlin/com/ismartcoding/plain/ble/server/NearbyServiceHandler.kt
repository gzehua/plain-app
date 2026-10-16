package com.ismartcoding.plain.ble.server

import com.ismartcoding.plain.ble.RustBleWire
import com.ismartcoding.plain.ble.BleUuids
import com.ismartcoding.plain.discover.RustNearbyWire
import com.ismartcoding.plain.discover.RustPairingRuntime
import com.ismartcoding.plain.enums.NearbyMessageType
import com.ismartcoding.plain.lib.logcat.LogCat

class NearbyServiceHandler : BleServiceHandler {
    override val charUuid: String = BleUuids.NEARBY_CHAR_UUID

    override suspend fun handleRequest(message: ByteArray, clientMac: String): ByteArray {
        val result = handleBody(RustBleWire.nearbyBody(message).decodeToString(throwOnInvalidSequence = true), clientMac)
        return RustBleWire.nearby((result ?: "").encodeToByteArray())
    }

    private suspend fun handleBody(body: String, clientMac: String): String? {
        val message = RustNearbyWire.parse(body) ?: run {
            return null
        }
        val (type, payload) = message
        LogCat.d("NearbyServiceHandler: type=$type from=$clientMac")

        return when (type) {
            NearbyMessageType.DISCOVER ->
                com.ismartcoding.plain.discover.RustDiscoveryAdvertisement.replyJson().toString()

            NearbyMessageType.DISCOVER_REPLY -> null

            NearbyMessageType.PAIR_REQUEST -> {
                RustPairingRuntime.receiveRequest(payload, clientMac)
                "1"
            }

            NearbyMessageType.PAIR_RESPONSE -> {
                RustPairingRuntime.complete(payload)
                "1"
            }

            NearbyMessageType.PAIR_CANCEL -> {
                RustPairingRuntime.receiveCancel(payload)
                "1"
            }
        }
    }
}

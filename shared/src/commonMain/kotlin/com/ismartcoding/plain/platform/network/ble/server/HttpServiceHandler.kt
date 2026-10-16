package com.ismartcoding.plain.platform.network.ble.server

import com.ismartcoding.plain.platform.network.ble.BleDataChannel
import com.ismartcoding.plain.platform.network.ble.BleUuids

class HttpServiceHandler : BleServiceHandler {
    override val charUuid: String = BleUuids.HTTP_CHAR_UUID
    override suspend fun handleRequest(message: ByteArray, clientMac: String): ByteArray =
        BleDataChannel.incoming(clientMac, charUuid, message)
}

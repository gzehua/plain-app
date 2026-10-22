package com.ismartcoding.plain.platform.network.ble.server

import com.ismartcoding.plain.rust.native.RustNativeApi
import com.ismartcoding.plain.platform.network.ble.BleUuids

class NearbyServiceHandler : BleServiceHandler {
    override val charUuid: String = BleUuids.NEARBY_CHAR_UUID

    override suspend fun handleRequest(message: ByteArray, clientMac: String): ByteArray =
        RustNativeApi.bleIncoming(clientMac, charUuid, message)
}

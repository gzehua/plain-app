package com.ismartcoding.plain.discover

import com.ismartcoding.plain.lib.crypto.sha256
import com.ismartcoding.plain.lib.extensions.toHexString

object RustBleServiceData {
    fun decode(data: ByteArray?): BleAdvertisement? {
        if (data == null || data.size < 9) return null
        val flags = data[0].toInt() and 0xff
        val shortId = data.copyOfRange(1, 9).toHexString()
        return BleAdvertisement(
            shortId = shortId,
            awareSupported = (flags and 1) != 0,
            awareRunning = (flags and 2) != 0,
        )
    }

    fun shortIdOf(clientId: String): String {
        require(clientId.isNotEmpty() && clientId.length <= 1024) { "Invalid peer identity" }
        return sha256(clientId.encodeToByteArray()).copyOfRange(0, 8).toHexString()
    }
}
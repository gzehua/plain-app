package com.ismartcoding.plain.platform.network.ble

import com.ismartcoding.plain.rust.native.RustBleBridge

import com.ismartcoding.plain.platform.system.PlatformLock

internal class RustBleEncoder(message: ByteArray, requestId: Int, response: Boolean, limit: Int) : BleEncoder {
    private val lock = PlatformLock()
    private var handle = RustBleBridge.encoder(message, requestId, response, limit)
    override fun next(): ByteArray? = lock.withLock {
        check(handle != 0L) { "BLE encoder closed" }
        RustBleBridge.call(4, handle, 0, 0, 0, false, ByteArray(0))
    }
    override fun close() = lock.withLock { if (handle != 0L) { RustBleBridge.free(handle); handle = 0 } }
}

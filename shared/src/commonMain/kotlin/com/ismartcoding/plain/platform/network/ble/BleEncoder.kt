package com.ismartcoding.plain.platform.network.ble

interface BleEncoder : AutoCloseable {
    fun next(): ByteArray?
}

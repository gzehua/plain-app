package com.ismartcoding.plain.features.chat.peer.transport

import com.ismartcoding.plain.platform.network.ble.RustBleWire
import com.ismartcoding.plain.platform.network.ble.BleServices
import com.ismartcoding.plain.platform.network.ble.client.BleDeviceApi
import com.ismartcoding.plain.helpers.Base64Lenient
import com.ismartcoding.plain.platform.system.PlatformLock
import com.ismartcoding.plain.platform.network.bleTransport
import com.ismartcoding.plain.platform.network.isBleReady
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.*

/**
 * Peer traffic over BLE — the App half of Rust-owned BLE transport.
 *
 * Rust decides *that* an exchange is needed and owns the framing, crypto and
 * validation; this object only performs the GATT I/O for one message. The
 * per-frame codec runs through [RustBleWire]'s synchronous JNI FFI (microsecond
 * class), so a chunked transfer never pays a cross-language round trip per
 * frame — Rust makes exactly one host call per message and receives the fully
 * assembled response bytes back.
 */
object BleTransport {
    private val lock = PlatformLock()
    private val mutexes = mutableMapOf<String, Mutex>()

    suspend fun exchange(params: JsonObject): JsonElement {
        check(isBleReady()) { "BLE not ready" }
        val shortId = params.getValue("shortId").jsonPrimitive.content
        val request = Base64Lenient.decode(params.getValue("request").jsonPrimitive.content)
        val scanner = bleTransport().createScanner()
        val client = scanner.createClient(shortId)
            ?: withTimeoutOrNull(10_000) { scanner.scan(com.ismartcoding.plain.platform.network.ble.BleUuids.SERVICE_UUID).first { it.id == shortId }; scanner.createClient(shortId) }
            ?: error("BLE device not found")
        val mutex = lock.withLock { mutexes.getOrPut(shortId) { Mutex() } }
        return mutex.withLock {
            val api = BleDeviceApi(client, RustBleWire)
            scanner.pauseScan()
            try {
                check(api.ensureConnected()) { "BLE connect failed" }
                val response = api.requestAsync(BleServices.http, request)
                buildJsonObject { put("response", JsonPrimitive(Base64Lenient.encode(response))) }
            } finally { scanner.resumeScan() }
        }
    }
}
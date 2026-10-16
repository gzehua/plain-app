package com.ismartcoding.plain.features.nearby


import com.ismartcoding.plain.platform.network.ble.RustBleWire
import com.ismartcoding.plain.platform.network.ble.BleServices
import com.ismartcoding.plain.platform.network.ble.BleUuids
import com.ismartcoding.plain.platform.network.ble.client.BleDeviceApi
import com.ismartcoding.plain.platform.network.ble.client.BleGattClient
import com.ismartcoding.plain.platform.network.ble.client.BleScanner
import com.ismartcoding.plain.features.nearby.NearbyMessageType
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.network.bleTransport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

object BleScanDriver {
    suspend fun scan() {
        try { feedScan() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Throwable) { LogCat.e("BLE scan driver", error) }
    }

    private suspend fun feedScan() {
        val scan = RustNearbyWire.beginScan()
        try {
            forEachAdvertisement { shortId ->
                try { RustNearbyWire.scanSeen(scan, shortId) }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { LogCat.d("[BLE] scanSeen $shortId: ${error.message}") }
            }
        } finally {
            withContext(NonCancellable) { RustNearbyWire.endScan(scan) }
        }
    }

    private suspend fun forEachAdvertisement(onSeen: suspend (String) -> Unit) {
        bleTransport().createScanner().scan(BleUuids.SERVICE_UUID).collect { device ->
            onSeen(device.id)
        }
    }

    /**
     * Answers Rust's `bleScanRead`: performs one GATT DISCOVER read on the
     * device with [shortId] and returns its raw nearby payload, or an empty
     * object when the device is unknown / the read fails.
     */
    suspend fun read(params: JsonObject): JsonElement {
        val shortId = params.getValue("shortId").jsonPrimitive.content
        val scanner = bleTransport().createScanner()
        val device = scanner.createClient(shortId) ?: return JsonObject(emptyMap())
        val payload = readDiscoverReply(scanner, device)
        return buildJsonObject { put("payload", payload?.let(::JsonPrimitive) ?: JsonPrimitive(null as String?)) }
    }

    /**
     * One GATT DISCOVER read. Scanning shares the Bluetooth radio with GATT, so
     * the scan loop is paused for the duration of the session.
     */
    private suspend fun readDiscoverReply(scanner: BleScanner, device: BleGattClient): String? {
        return try {
            scanner.pauseScan()
            val api = BleDeviceApi(device, RustBleWire)
            api.ensureConnected()
            if (!api.isConnected()) return null
            val message = RustBleWire.nearby(RustNearbyWire.encode(NearbyMessageType.DISCOVER, "").encodeToByteArray())
            val result = api.requestAsync(BleServices.nearby, message)
            api.disconnect()
            RustBleWire.nearbyBody(result).decodeToString(throwOnInvalidSequence = true).takeIf { it.isNotEmpty() }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            LogCat.e("[BLE] bleScanRead error: ${e.message}")
            null
        } finally {
            scanner.resumeScan()
            scanner.teardownConnection(device)
        }
    }
}

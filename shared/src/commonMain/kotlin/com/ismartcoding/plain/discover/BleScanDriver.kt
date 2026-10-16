package com.ismartcoding.plain.discover

import com.ismartcoding.plain.chat.peer.PeerCacher

import com.ismartcoding.plain.ble.RustBleWire
import com.ismartcoding.plain.ble.BleServices
import com.ismartcoding.plain.ble.BleUuids
import com.ismartcoding.plain.ble.client.BleDeviceApi
import com.ismartcoding.plain.ble.client.BleGattClient
import com.ismartcoding.plain.ble.client.BleScanner
import com.ismartcoding.plain.enums.NearbyMessageType
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.bleTransport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * BLE scan driver — the App half of Rust-owned BLE discovery.
 *
 * The App runs the radio loop and nothing else: each discovered advertisement
 * is reduced to its shortId and handed to Rust via [RustNearbyWire.scanSeen].
 * Rust decides when a GATT read is due, then calls back into [read] over the
 * host socket to perform exactly one DISCOVER read. The resulting device is
 * published by Rust over `NEARBY_DEVICES_UPDATED` / `WS_NEARBY_DEVICE_FOUND`,
 * so this class never touches the discovered-device list.
 */
object BleScanDriver {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var scanJob: Job? = null

    /** Whether the App-side BLE scan loop is currently running. */
    var isScanning: Boolean = false
        private set

    /**
     * Starts the radio loop. Safe to call repeatedly — a running loop is kept.
     * Returns immediately; the loop feeds Rust asynchronously.
     */
    fun start() {
        if (scanJob?.isActive == true) return
        scanJob = scope.launch {
            isScanning = true
            try {
                feedScan()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                LogCat.e("BLE scan driver", error)
            } finally {
                isScanning = false
            }
        }
    }

    fun stop() {
        scanJob?.cancel()
        scanJob = null
        isScanning = false
    }

    /**
     * The radio loop: for every advertised device, hand its shortId to Rust.
     * Rust replies with a step, but the decision to read is now Rust's — the
     * App simply ignores the step and lets the worker drive reads via [read].
     */
    private suspend fun feedScan() {
        val scan = RustNearbyWire.beginScan()
        try {
            forEachAdvertisement { shortId ->
                // Feed Rust and let it decide. A thrown error here must not
                // kill the loop; skip the advertisement and keep scanning.
                runCatching { RustNearbyWire.scanSeen(scan, shortId) }
                    .onFailure { LogCat.d("[BLE] scanSeen $shortId: ${it.message}") }
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

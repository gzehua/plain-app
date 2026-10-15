package com.ismartcoding.plain.discover

import com.ismartcoding.plain.ble.client.BleGattClient
import com.ismartcoding.plain.data.DNearbyDevice
import com.ismartcoding.plain.graphql.models.NearbyDevice
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.ui.models.NearbyViewModel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

private const val NEARBY_DEVICE_FIELDS =
    "id name ips port deviceType version platform lastSeen discoveryMethods bestIp status"

object RustNearbyDevices {
    private val mutex = Mutex()
    private val clients = mutableMapOf<String, BleGattClient>()

    internal suspend fun client(id: String): BleGattClient? = mutex.withLock { clients[id] }

    internal suspend fun rememberClient(id: String, client: BleGattClient) = mutex.withLock {
        clients[id] = client
    }

    /**
     * Kicks off the background scan and returns the cached list so the
     * caller can populate the UI without waiting for the first event.
     */
    suspend fun startDiscovery(): List<NearbyDevice> {
        PlainRust.mainMutate("startDiscovery")
        return snapshot()
    }

    suspend fun stopDiscovery(): Boolean =
        PlainRust.mainMutate("stopDiscovery")
            .getValue("stopDiscovery").jsonPrimitive.content == "true"

    /**
     * Pulls the current cache. Used by the `NEARBY_DEVICES_UPDATED` event
     * handler — the event payload only carries the revision.
     */
    suspend fun snapshot(): List<NearbyDevice> =
        PlainRust.mainQuery("nearbyDevices { $NEARBY_DEVICE_FIELDS }")
            .getValue("nearbyDevices")
            .jsonArray
            .map { JsonHelper.jsonDecode<NearbyDevice>(it.toString()) }

    fun apply(devices: List<NearbyDevice>) {
        val mapped = devices.map { device ->
            DNearbyDevice(
                id = device.id,
                name = device.name,
                ips = device.ips,
                port = device.port,
                deviceType = runCatching { com.ismartcoding.plain.enums.DeviceType.valueOf(device.deviceType) }
                    .getOrDefault(com.ismartcoding.plain.enums.DeviceType.OTHER),
                version = device.version,
                platform = device.platform,
                lastSeen = runCatching { kotlin.time.Instant.parse(device.lastSeen) }
                    .getOrDefault(kotlin.time.Instant.fromEpochMilliseconds(0)),
                status = NearbyViewModel.getStatus(device.id, device.status == "PAIRED"),
                discoveryMethods = device.discoveryMethods
                    .mapNotNull { runCatching { com.ismartcoding.plain.enums.DiscoveryMethod.valueOf(it) }.getOrNull() }
                    .toSet(),
                bestIp = device.bestIp,
                bleClient = clients[device.id],
            )
        }
        clients.keys.retainAll(mapped.map { it.id }.toSet())
        NearbyViewModel.nearbyDevices.value = mapped
    }
}
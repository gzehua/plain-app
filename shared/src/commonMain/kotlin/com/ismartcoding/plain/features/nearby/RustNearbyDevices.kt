package com.ismartcoding.plain.features.nearby

import kotlinx.serialization.json.JsonObject

import com.ismartcoding.plain.platform.network.ble.client.BleGattClient
import com.ismartcoding.plain.api.graphql.models.NearbyDevice
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.ui.nearby.NearbyViewModel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

private const val NEARBY_DEVICE_FIELDS =
    "id name ips port deviceType version platform lastSeen discoveryMethods bestIp status"

object RustNearbyDevices {
    internal fun client(id: String): BleGattClient? =
        com.ismartcoding.plain.platform.network.bleTransport().createScanner().createClient(RustBleServiceData.shortIdOf(id))

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
        NearbyViewModel.nearbyDevices.value = devices
    }
}

package com.ismartcoding.plain.features.nearby


import com.ismartcoding.plain.platform.network.ble.client.BleGattClient
import com.ismartcoding.plain.api.graphql.models.NearbyDevice
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.ui.nearby.NearbyViewModel
import kotlinx.serialization.json.jsonArray

private const val NEARBY_DEVICE_FIELDS =
    "id name ips port deviceType version platform lastSeen discoveryMethods bestIp status"

object RustNearbyDevices {
    internal fun client(id: String): BleGattClient? =
        com.ismartcoding.plain.platform.network.bleTransport().createScanner().createClient(RustBleServiceData.shortIdOf(id))

    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    suspend fun observeDiscovery() {
        val token = kotlin.uuid.Uuid.random().toString()
        discoverySession(token,
            acquire = { PlainRust.mainMutate("acquireDiscovery(token: ${com.ismartcoding.plain.rust.request.gql(it)})"); apply(snapshot()) },
            release = { PlainRust.mainMutate("releaseDiscovery(token: ${com.ismartcoding.plain.rust.request.gql(it)})") },
        )
    }

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

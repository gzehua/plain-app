package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi


import com.ismartcoding.plain.models.NearbyDevice
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.ui.nearby.NearbyViewModel
import kotlinx.serialization.json.jsonArray

private const val NEARBY_DEVICE_FIELDS =
    "id name ips port deviceType version platform lastSeen discoveryMethods bestIp status"

object RustNearbyDevices {
    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    suspend fun observeDiscovery() {
        val token = kotlin.uuid.Uuid.random().toString()
        discoverySession(token,
            acquire = { RustGraphqlApi.mutate("acquireDiscovery(token: ${com.ismartcoding.plain.rust.graphql.gql(it)})"); apply(snapshot()) },
            release = { RustGraphqlApi.mutate("releaseDiscovery(token: ${com.ismartcoding.plain.rust.graphql.gql(it)})") },
        )
    }

    /**
     * Pulls the current cache. Used by the `NEARBY_DEVICES_UPDATED` event
     * handler — the event payload only carries the revision.
     */
    suspend fun snapshot(): List<NearbyDevice> =
        RustGraphqlApi.query("nearbyDevices { $NEARBY_DEVICE_FIELDS }")
            .getValue("nearbyDevices")
            .jsonArray
            .map { JsonHelper.jsonDecode<NearbyDevice>(it.toString()) }

    fun apply(devices: List<NearbyDevice>) {
        NearbyViewModel.nearbyDevices.value = devices
    }
}

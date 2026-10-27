package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.NearbyDevice
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.ui.nearby.NearbyViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object RustNearbyDevices {
    @OptIn(ExperimentalUuidApi::class)

    suspend fun observeDiscovery() {
        val token = Uuid.random().toString()
        discoverySession(token,
            acquire = {
                RustGraphqlApi.execute<Unit>(Mutation.acquireDiscovery(it))
                apply(snapshot())
                RustMdnsRuntime.refresh()
            },
            release = { RustGraphqlApi.execute<Unit>(Mutation.releaseDiscovery(it)) },
        )
    }
    /**
     * Pulls the current cache. Used by the `NEARBY_DEVICES_UPDATED` event
     * handler — the event payload only carries the revision.
     */

    suspend fun snapshot(): List<NearbyDevice> =
        RustGraphqlApi.execute(Query.nearbyDevices())

    fun apply(devices: List<NearbyDevice>) {
        NearbyViewModel.nearbyDevices.value = devices
    }
}

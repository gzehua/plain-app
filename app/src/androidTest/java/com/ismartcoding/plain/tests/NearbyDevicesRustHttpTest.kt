package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.data.DNearbyDevice
import com.ismartcoding.plain.discover.NearbyDeviceCache
import com.ismartcoding.plain.discover.RustNearbyDevices
import com.ismartcoding.plain.enums.DeviceType
import com.ismartcoding.plain.enums.DiscoveryMethod
import com.ismartcoding.plain.ui.models.NearbyViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import kotlin.time.Clock

@RunWith(AndroidJUnit4::class)
class NearbyDevicesRustHttpTest {
    @Test
    fun nearbyHistoryRoundTripsInRustAndSnapshotProjectionRemovesStaleUiRows() = runBlocking {
        val id = "synthetic-nearby-list-${UUID.randomUUID()}"
        val device = DNearbyDevice(id, id, listOf("192.0.2.1"), 2443, DeviceType.PHONE, "1", "test", Clock.System.now(), discoveryMethods = setOf(DiscoveryMethod.LAN))
        try {
            NearbyDeviceCache.upsertAsync(device)
            val stored = NearbyDeviceCache.getAllAsync().single { it.id == id }
            assertEquals(device.ips, stored.ips)
            assertEquals(device.name, stored.name)
            assertEquals(device.port, stored.port)
            NearbyViewModel.nearbyDevices.value = listOf(stored)
            NearbyDeviceCache.removeAsync(id)
            RustNearbyDevices.apply(RustNearbyDevices.snapshot())
            assertFalse(NearbyViewModel.nearbyDevices.value.any { it.id == id })
            assertFalse(NearbyDeviceCache.getAllAsync().any { it.id == id })
        } finally {
            NearbyDeviceCache.removeAsync(id)
            RustNearbyDevices.apply(RustNearbyDevices.snapshot())
        }
    }
}

package com.ismartcoding.plain.tests

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.rust.native.RustNativeApi

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.features.nearby.RustMdnsRuntime
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import com.ismartcoding.plain.rust.host.network.MdnsMulticastHost

@RunWith(AndroidJUnit4::class)
class MdnsRuntimeRustHttpTest {
    private suspend fun awaitReceiver() = withTimeout(10_000) {
        while (!RustMdnsRuntime.snapshot().getValue("receiver").jsonPrimitive.boolean) delay(50)
    }
    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun closedPhysicalLeaseCannotAcquireAfterReleaseOrReplaceTheCurrentOwner() = runBlocking {
        awaitReceiver()
        val closed = Uuid.random().toString()
        fun physical(acquire: Boolean, lease: String) = MdnsMulticastHost.handle(buildJsonObject {
            put("acquire", acquire); put("lease", lease)
        }).encodeReply(0).let { Json.parseToJsonElement(it).jsonObject.getValue("result").jsonPrimitive.boolean }
        assertTrue(physical(false, closed))
        assertFalse(physical(true, closed))
        assertFalse(physical(true, Uuid.random().toString()))
        assertTrue(RustMdnsRuntime.snapshot().getValue("receiver").jsonPrimitive.boolean)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun rustControlsReceiverAndScannerWithActualAndroidMulticastPermission() = runBlocking {
        awaitReceiver()
        suspend fun snapshot() = RustMdnsRuntime.snapshot()
        val before = snapshot()
        val debugToken = Uuid.random().toString()
        try {
            suspend fun discovering(): Boolean =
                RustGraphqlApi.query("isDiscovering").getValue("isDiscovering").jsonPrimitive.boolean
            assertEquals(true, RustGraphqlApi.mutate("startDiscovery").getValue("startDiscovery").jsonPrimitive.boolean)
            assertTrue(discovering())
            val started = snapshot()
            assertTrue(started.getValue("receiver").jsonPrimitive.boolean)
            assertTrue(started.getValue("scanning").jsonPrimitive.boolean)
            assertTrue(started.getValue("revision").jsonPrimitive.long > before.getValue("revision").jsonPrimitive.long)
            assertEquals(true, RustGraphqlApi.mutate("stopDiscovery").getValue("stopDiscovery").jsonPrimitive.boolean)
            assertFalse(discovering())
            val stopped = snapshot()
            assertFalse(stopped.getValue("scanning").jsonPrimitive.boolean)
            assertTrue(stopped.getValue("receiver").jsonPrimitive.boolean)
            assertTrue(stopped.getValue("revision").jsonPrimitive.long > started.getValue("revision").jsonPrimitive.long)
            assertTrue(snapshot().getValue("revision").jsonPrimitive.long >= stopped.getValue("revision").jsonPrimitive.long)
            RustMdnsRuntime.debugStart(debugToken)
            assertTrue(snapshot().getValue("capturing").jsonPrimitive.boolean)
            val captured = RustMdnsRuntime.snapshot()
            val packets = RustMdnsRuntime.packets(captured, false)
            assertEquals(captured.getValue("running").jsonPrimitive.boolean, RustMdnsRuntime.running)
            if (RustNativeApi.lanIpv4Addresses().isEmpty()) {
                assertTrue(packets.all { it.srcPort > 0 && it.size > 0 })
            } else assertTrue(packets.any { it.summary.contains("PTR") && it.detail.contains("_plainapp._tcp.local") && it.srcPort > 0 && it.size > 0 })
            assertTrue(RustMdnsRuntime.services(captured).all { it.serviceType == "_plainapp._tcp.local" })
            RustGraphqlApi.mutate("stopDiscovery")
            assertTrue(discovering())
            RustMdnsRuntime.debugStop(debugToken).await()
            val cleaned = snapshot()
            assertFalse(cleaned.getValue("scanning").jsonPrimitive.boolean)
            assertFalse(cleaned.getValue("capturing").jsonPrimitive.boolean)
            assertTrue(RustMdnsRuntime.packets(cleaned, false).isEmpty())
        } finally {
            RustMdnsRuntime.debugStop(debugToken).await()
            RustGraphqlApi.mutate(if (before.getValue("manualScanning").jsonPrimitive.boolean) "startDiscovery" else "stopDiscovery")
        }
    }
}

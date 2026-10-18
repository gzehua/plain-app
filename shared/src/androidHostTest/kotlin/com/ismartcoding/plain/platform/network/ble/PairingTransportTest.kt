package com.ismartcoding.plain.platform.network.ble

import com.ismartcoding.plain.platform.network.ble.server.BleGattServer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.put
import kotlin.io.encoding.Base64
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PairingTransportTest {
    private var connection: PairingTransport.Connection? = null

    @AfterTest
    fun close() {
        connection?.close()
    }

    private class Server : BleGattServer {
        val starts = mutableListOf<Pair<ByteArray, Long>>()
        val refreshes = mutableListOf<Pair<ByteArray, Long>>()
        var stops = 0
        var failStart = false
        override fun start(payload: ByteArray, revision: Long) {
            starts += payload.copyOf() to revision
            if (failStart) error("SDK rejected advertising")
        }
        override fun refreshAdvertising(payload: ByteArray, revision: Long) { refreshes += payload.copyOf() to revision }
        override fun stop() { stops++ }
        override fun sendNotification(mac: String, charUuid: String, value: ByteArray): Boolean = true
        override suspend fun sendNotificationBlocking(mac: String, charUuid: String, value: ByteArray): Boolean = true
    }

    private fun command(revision: Long, active: Boolean = true, payload: ByteArray = if (active) ByteArray(9) { it.toByte() } else byteArrayOf()): JsonObject = buildJsonObject {
        put("lease", checkNotNull(connection).lease)
        put("revision", revision)
        put("active", active)
        put("bytes", Base64.encode(payload))
    }

    @Test
    fun sdkReceivesExactRustBytesAndOldRevisionsNeverRestartIt() {
        val sdk = Server()
        val current = PairingTransport.connect { sdk }.also { connection = it }
        val bytes = ByteArray(9) { (it + 1).toByte() }
        assertTrue(current.handle(command(1, payload = bytes)).jsonPrimitive.boolean)
        assertContentEquals(bytes, sdk.starts.single().first)
        assertEquals(1L, sdk.starts.single().second)
        assertFalse(current.handle(command(1)).jsonPrimitive.boolean)
        assertEquals(1, sdk.starts.size)
        assertTrue(sdk.refreshes.isEmpty())
        assertTrue(current.handle(command(2, payload = bytes.reversedArray())).jsonPrimitive.boolean)
        assertContentEquals(bytes.reversedArray(), sdk.refreshes.single().first)
        assertTrue(current.handle(command(3, active = false)).jsonPrimitive.boolean)
        assertFalse(PairingTransport.isAdvertising())
        assertEquals(1, sdk.stops)
        assertFalse(current.handle(command(2)).jsonPrimitive.boolean)
        assertEquals(1, sdk.starts.size)
    }

    @Test
    fun oldHostCleanupCannotCloseItsReplacement() {
        val firstSdk = Server()
        val first = PairingTransport.connect { firstSdk }.also { connection = it }
        first.handle(command(1))
        val staleCommand = command(100)
        val secondSdk = Server()
        val second = PairingTransport.connect { secondSdk }.also { connection = it }
        assertEquals(1, firstSdk.stops)
        second.handle(command(2))
        assertFailsWith<IllegalStateException> { second.handle(staleCommand) }
        assertTrue(secondSdk.refreshes.isEmpty())
        first.close()
        assertEquals(0, secondSdk.stops)
        assertTrue(PairingTransport.isAdvertising())
        assertFailsWith<IllegalStateException> { first.handle(command(3)) }
        assertEquals(1, secondSdk.starts.size)
        second.close()
        assertEquals(1, secondSdk.stops)
        assertFalse(PairingTransport.isAdvertising())
        assertFailsWith<IllegalStateException> { second.handle(command(4)) }
    }

    @Test
    fun malformedCommandsAndFailedCreationDoNotLeaveAnOwnedServer() {
        val failed = Server().also { it.failStart = true }
        var creations = 0
        val replacement = Server()
        val current = PairingTransport.connect { if (creations++ == 0) failed else replacement }.also { connection = it }
        assertFailsWith<IllegalArgumentException> { current.handle(command(1, payload = byteArrayOf(1))) }
        assertEquals(0, creations)
        assertFailsWith<IllegalStateException> { current.handle(command(1)) }
        assertEquals(1, failed.stops)
        assertFalse(PairingTransport.isAdvertising())
        current.handle(command(2))
        assertTrue(PairingTransport.isAdvertising())
        assertEquals(1, replacement.starts.size)
    }
}

package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.events.EventType
import com.ismartcoding.plain.events.HConfirmToAcceptLoginEvent
import com.ismartcoding.plain.features.session.AuthRequest
import com.ismartcoding.plain.features.session.RustSessionStore
import com.ismartcoding.plain.features.session.RustWebLogin
import com.ismartcoding.plain.helpers.Base64Lenient
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.*
import com.ismartcoding.plain.preferences.*
import com.ismartcoding.plain.rust.PlainRust
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SessionMigrationRustHttpTest {
    @Test
    fun encryptedLoginConfirmationSessionUiAndRevocationUseRust() = runBlocking {
        val oldService = UserPrefs.service.value
        val oldDesktop = UserPrefs.desktopAccess.value
        val oldTwoFactor = RustSystemState.state.value.authTwoFactor
        val id = "session-fixture-${UUID.randomUUID()}"
        var pendingId = ""
        val http = createPeerStatusHttpClient()
        try {
            UserPrefs.service.value = true
            UserPrefs.desktopAccess.value = true
            RustSystemState.setTwoFactor(true)
            startHttpEngineAsync()
            val client = generateECDHKeyPair()
            val digest = sha512(RustSystemState.state.value.password.encodeToByteArray())
            val passwordKey = digest.take(32).encodeToByteArray()
            val request = AuthRequest(digest, "Fixture", "1", "Android", "1", true,
                Base64Lenient.encode(client.publicKeyEncoded))
            val notification = async(start = CoroutineStart.UNDISPATCHED) {
                withTimeout(10_000) {
                    PlainRust.events.first {
                        it.type == EventType.WEB_LOGIN_REQUESTED.name &&
                            JsonHelper.jsonDecode<JsonObject>(it.payload)["clientId"]?.jsonPrimitive?.content == id
                    }
                }
            }
            http.webSocket("ws://127.0.0.1:${UserPrefs.httpPort.value}/?cid=$id&auth=1") { socket ->
                socket.sendBinary(chaCha20Encrypt(passwordKey, JsonHelper.jsonEncode(request)))
                val pending = JsonHelper.jsonDecode<JsonObject>(requireNotNull(chaCha20Decrypt(passwordKey,
                    requireNotNull(withTimeout(10_000) { socket.incoming.receive() }.binary))).decodeToString())
                assertEquals("PENDING", pending.getValue("status").jsonPrimitive.content)
                val facts = JsonHelper.jsonDecode<JsonObject>(notification.await().payload)
                pendingId = facts.getValue("requestId").jsonPrimitive.content
                val event = HConfirmToAcceptLoginEvent(id,
                    JsonHelper.jsonDecodeFromElement<AuthRequest>(facts.getValue("request")),
                    pendingId, facts.getValue("clientIp").jsonPrimitive.content)
                assertEquals("", event.request.password)
                RustWebLogin.complete(event)
                val response = JsonHelper.jsonDecode<JsonObject>(requireNotNull(chaCha20Decrypt(passwordKey,
                    requireNotNull(withTimeout(10_000) { socket.incoming.receive() }.binary))).decodeToString())
                assertEquals("COMPLETED", response.getValue("status").jsonPrimitive.content)
                val shared = computeECDHSharedKey(client.privateKeyEncoded,
                    Base64Lenient.decode(response.getValue("ecdhPublicKey").jsonPrimitive.content))
                val session = RustSessionStore.list().single { it.clientId == id }
                assertEquals("Fixture", session.browserName)
                assertFalse(session.isCustom)
                assertArrayEquals(Base64Lenient.decode(requireNotNull(shared)), RustSessionStore.list().firstOrNull { it.clientId == id }?.token?.let(Base64Lenient::decode))
                assertTrue(renameSessionListItemAsync(id, "Renamed fixture"))
                assertEquals("Renamed fixture", fetchSessionsListItemsAsync().single { it.clientId == id }.name)
                assertTrue(runCatching { RustWebLogin.complete(event) }.isFailure)
            }
            deleteSessionListItemAsync(id)
            assertNull(RustSessionStore.list().firstOrNull { it.clientId == id }?.token?.let(Base64Lenient::decode))
        } finally {
            http.close()
            if (pendingId.isNotEmpty()) RustWebLogin.cancel(pendingId)
            RustSessionStore.delete(id)
            RustSystemState.setTwoFactor(oldTwoFactor)
            UserPrefs.desktopAccess.value = oldDesktop
            UserPrefs.service.value = oldService
        }
    }
}

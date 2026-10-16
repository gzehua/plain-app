package com.ismartcoding.plain.tests

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import android.os.Environment
import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.app.startHttpEngineAsync
import com.ismartcoding.plain.app.stopHttpEngineAsync
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.preferences.SystemCommand
import com.ismartcoding.plain.preferences.SystemPrefsClient
import com.ismartcoding.plain.preferences.UserPrefs
import com.ismartcoding.plain.rust.PlainRust
import java.io.File
import java.security.SecureRandom
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.*
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApiFixtureRustHttpTest {
    @Test
    fun serveDisposableApiFixtures() = runBlocking {
        check(appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0)
        check(Build.HARDWARE == "ranchu" || Build.HARDWARE == "goldfish")
        check(InstrumentationRegistry.getArguments().getString("plainApiFixture") == "isolated")
        val ready = File(appContext.filesDir, "api-test-session.json")
        val stop = File(appContext.filesDir, "api-test-stop")
        ready.delete()
        stop.delete()
        val root = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "PlainApiFixture/${UUID.randomUUID()}")
        val peerId = "api-fixture-${UUID.randomUUID()}"
        var clientId = ""
        val oldAccess = UserPrefs.desktopAccess.value
        UserPrefs.desktopAccess.value = true
        startHttpEngineAsync()
        SystemPrefsClient.local.refresh()
        val oldPermissions = SystemPrefsClient.local.system.value.apiPermissions
        try {
            SystemPrefsClient.local.updateSystem(SystemCommand.SetApiPermissions(setOf(Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_MEDIA_IMAGES, Permission.READ_MEDIA_VIDEOS, Permission.READ_MEDIA_AUDIO, Permission.READ_CONTACTS, Permission.WRITE_CONTACTS, Permission.READ_CALL_LOG, Permission.WRITE_CALL_LOG, Permission.READ_SMS, Permission.SEND_SMS, Permission.QUERY_ALL_PACKAGES, Permission.NOTIFICATION_LISTENER).map { it.name }.toSet()))
            assertTrue(root.mkdirs())
            val session = PlainRust.mainMutate("createSession(name: \"API emulator fixture\") { clientId token }").getValue("createSession").jsonObject
            clientId = session.getValue("clientId").jsonPrimitive.content
            val randomKey = { Base64.encodeToString(ByteArray(32).also { SecureRandom().nextBytes(it) }, Base64.NO_WRAP) }
            val peer = buildJsonObject {
                put("id", peerId)
                put("name", "API fixture peer")
                put("host", "127.0.0.1:1")
                put("deviceType", "OTHER")
                put("token", randomKey())
                put("signaturePublicKey", randomKey())
                put("chatKey", randomKey())
            }
            check(PlainRust.mainGraphql("mutation(\$input: PeerLoginInput!) { loginPeer(input: \$input) }", mapOf("input" to peer)).getValue("loginPeer").jsonPrimitive.boolean)
            check(PlainRust.mainQuery("peerRecord(id: ${JsonPrimitive(peerId)}) { id }").getValue("peerRecord").jsonObject.getValue("id").jsonPrimitive.content == peerId)
            val app = PlainRust.mainQuery("app { httpPort capabilities }").getValue("app").jsonObject
            val credentials = buildJsonObject {
                put("clientId", clientId)
                put("token", session.getValue("token"))
                put("port", app.getValue("httpPort"))
                put("capabilities", app.getValue("capabilities"))
                put("testRoot", root.path)
                put("dataDir", appContext.filesDir.path)
                put("cacheDir", appContext.cacheDir.path)
            }.toString()
            appContext.openFileOutput(ready.name, Context.MODE_PRIVATE).bufferedWriter().use { it.write(credentials) }
            withTimeout(10 * 60 * 1000L) { while (!stop.exists()) delay(250) }
        } finally {
            ready.delete()
            stop.delete()
            if (clientId.isNotEmpty()) PlainRust.mainMutate("revokeSession(clientId: ${JsonPrimitive(clientId)})")
            PlainRust.mainMutate("removePeerRecord(id: ${JsonPrimitive(peerId)})")
            root.deleteRecursively()
            SystemPrefsClient.local.updateSystem(SystemCommand.SetApiPermissions(oldPermissions))
            UserPrefs.desktopAccess.value = oldAccess
            stopHttpEngineAsync()
        }
    }
}

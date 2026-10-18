package com.ismartcoding.plain.tests

import android.content.Context
import android.content.pm.ApplicationInfo
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import android.os.Environment
import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.app.startHttpEngineAsync
import com.ismartcoding.plain.app.stopHttpEngineAsync
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.preferences.SystemPrefsClient
import com.ismartcoding.plain.preferences.UserPrefs
import com.ismartcoding.plain.rust.PlainRust
import java.io.File
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@Serializable
private data class ApiPeerFixture(val id: String, val key: String, val publicKey: String)

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
        val peerFile = File(appContext.filesDir, "api-test-peer.json")
        val peerFixture = JsonHelper.jsonDecode<ApiPeerFixture>(peerFile.readText())
        peerFile.delete()
        check(peerFixture.id.startsWith("api-fixture-"))
        UUID.fromString(peerFixture.id.removePrefix("api-fixture-"))
        check(Base64.decode(peerFixture.key, Base64.NO_WRAP).size == 32)
        check(Base64.decode(peerFixture.publicKey, Base64.NO_WRAP).size == 32)
        val peerId = peerFixture.id
        var channelId = ""
        var clientId = ""
        var peerCreated = false
        var rootCreated = false
        val oldAccess = UserPrefs.desktopAccess.value
        UserPrefs.desktopAccess.value = true
        startHttpEngineAsync()
        SystemPrefsClient.local.refresh()
        val oldPermissions = SystemPrefsClient.local.system.value.apiPermissions
        try {
            SystemPrefsClient.local.set("api_permissions", JsonArray(setOf(Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_MEDIA_IMAGES, Permission.READ_MEDIA_VIDEOS, Permission.READ_MEDIA_AUDIO, Permission.READ_CONTACTS, Permission.WRITE_CONTACTS, Permission.READ_CALL_LOG, Permission.WRITE_CALL_LOG, Permission.READ_SMS, Permission.SEND_SMS, Permission.QUERY_ALL_PACKAGES, Permission.NOTIFICATION_LISTENER).map { JsonPrimitive(it.name) }))
            assertTrue(root.mkdirs())
            rootCreated = true
            val session = PlainRust.mainMutate("createSession(name: \"API emulator fixture\") { clientId token }").getValue("createSession").jsonObject
            clientId = session.getValue("clientId").jsonPrimitive.content
            val database = File(appContext.filesDir, "plain.db")
            check(database.isFile && database.canonicalFile.parentFile == appContext.filesDir.canonicalFile)
            SQLiteDatabase.openDatabase(database.path, null, SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.NO_LOCALIZED_COLLATORS).use { db ->
                check(db.version == 1)
                val now = java.time.Instant.now().toString()
                db.execSQL(
                    "INSERT INTO peers (id,name,ip,key,public_key,status,port,device_type,created_at,updated_at) VALUES (?,?,?,?,?,?,?,?,?,?)",
                    arrayOf<Any>(peerId, "API fixture peer", "127.0.0.1", peerFixture.key, peerFixture.publicKey, "PAIRED", 1, "OTHER", now, now),
                )
                peerCreated = true
            }
            check(PlainRust.mainQuery("peers { id }").getValue("peers").jsonArray.count { it.jsonObject.getValue("id").jsonPrimitive.content == peerId } == 1)
            channelId = PlainRust.mainMutate("createChatChannel(name: \"API fixture channel\") { id }").getValue("createChatChannel").jsonObject.getValue("id").jsonPrimitive.content
            val channelKey = PlainRust.mainQuery("chatChannels { id key }").getValue("chatChannels").jsonArray.single { it.jsonObject.getValue("id").jsonPrimitive.content == channelId }.jsonObject.getValue("key")
            val app = PlainRust.mainQuery("app { httpPort capabilities }").getValue("app").jsonObject
            val credentials = JsonHelper.jsonEncode(mapOf(
                "clientId" to JsonPrimitive(clientId),
                "token" to session.getValue("token"),
                "port" to app.getValue("httpPort"),
                "capabilities" to app.getValue("capabilities"),
                "testRoot" to JsonPrimitive(root.path),
                "dataDir" to JsonPrimitive(appContext.filesDir.path),
                "cacheDir" to JsonPrimitive(appContext.cacheDir.path),
                "peerId" to JsonPrimitive(peerId),
                "peerChannelId" to JsonPrimitive(channelId),
                "peerChannelKey" to channelKey,
            ))
            appContext.openFileOutput(ready.name, Context.MODE_PRIVATE).bufferedWriter().use { it.write(credentials) }
            withTimeout(10 * 60 * 1000L) { while (!stop.exists()) delay(250) }
        } finally {
            ready.delete()
            stop.delete()
            if (clientId.isNotEmpty()) PlainRust.mainMutate("revokeSession(clientId: ${JsonPrimitive(clientId)})")
            if (channelId.isNotEmpty()) PlainRust.mainMutate("deleteChatChannel(id: ${JsonPrimitive(channelId)})")
            if (peerCreated) PlainRust.mainMutate("deletePeer(id: ${JsonPrimitive(peerId)})")
            if (rootCreated) root.deleteRecursively()
            SystemPrefsClient.local.set("api_permissions", JsonArray(oldPermissions.map(::JsonPrimitive)))
            UserPrefs.desktopAccess.value = oldAccess
            stopHttpEngineAsync()
        }
    }
}

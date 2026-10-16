package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.features.session.RustSessionStore
import com.ismartcoding.plain.platform.getDeviceCapabilities
import com.ismartcoding.plain.preferences.UserPrefs
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.ContentApiSession
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainGraphqlRustHttpTest {
    @Test
    fun jniAndAuthenticatedHttpUseOneSchemaAndKeepCredentialReadsLocal() = runBlocking {
        val name = "main-contract-${UUID.randomUUID()}"
        val oldService = UserPrefs.service.value
        val oldAccess = UserPrefs.desktopAccess.value
        var id: String? = null
        try {
            UserPrefs.service.value = true
            UserPrefs.desktopAccess.value = true
            val created = PlainRust.mainGraphql("mutation(\$name:String!){createSession(name:\$name){clientId token}}", mapOf("name" to JsonPrimitive(name))).getValue("createSession").jsonObject
            val clientId = created.getValue("clientId").jsonPrimitive.content
            id = clientId
            val token = created.getValue("token").jsonPrimitive.content
            assertTrue(token.isNotEmpty())
            val app = PlainRust.mainQuery("app { httpPort urlToken capabilities }").getValue("app").jsonObject
            assertEquals(getDeviceCapabilities().map { it.name }.toSet(), app.getValue("capabilities").jsonArray.map { it.jsonPrimitive.content }.toSet())
            val remote = ContentApiSession("http://127.0.0.1:${app.getValue("httpPort").jsonPrimitive.int}", clientId, token)
            val schema = "__schema { queryType { name } mutationType { name } }"
            assertEquals(PlainRust.mainQuery(schema), PlainRust.mainQuery(schema, remote))
            assertEquals(app.getValue("urlToken"), PlainRust.mainQuery("app { urlToken }", remote).getValue("app").jsonObject.getValue("urlToken"))
            val localPrefs = PlainRust.mainQuery("systemPrefs").getValue("systemPrefs").jsonObject
            val remotePrefs = PlainRust.mainQuery("systemPrefs", remote).getValue("systemPrefs").jsonObject
            assertEquals(app.getValue("urlToken"), localPrefs.getValue("url_token"))
            for (key in listOf("password", "url_token", "adb_token", "dev_token")) assertFalse(remotePrefs.containsKey(key))
            assertTrue(runCatching { PlainRust.mainQuery("sessions { token }", remote) }.isFailure)
            assertTrue(runCatching { PlainRust.mainQuery("systemSettings { password }", remote) }.isFailure)
            assertTrue(PlainRust.mainQuery("sessions { clientId }", remote).getValue("sessions").jsonArray.any { it.jsonObject.getValue("clientId").jsonPrimitive.content == clientId })
            assertTrue(PlainRust.mainGraphql("mutation(\$id:String!,\$name:String!){renameSession(clientId:\$id,name:\$name)}", mapOf("id" to JsonPrimitive(clientId), "name" to JsonPrimitive("$name renamed")), remote).getValue("renameSession").jsonPrimitive.boolean)
            assertEquals("$name renamed", RustSessionStore.list().single { it.clientId == clientId }.name)
        } finally {
            id?.let { RustSessionStore.delete(it) }
            UserPrefs.service.value = oldService
            UserPrefs.desktopAccess.value = oldAccess
        }
    }

    @Test
    fun selectedDeviceFieldsReadActualSdkFactsThroughTheMainNativeExecutor() = runBlocking {
        val data = PlainRust.mainQuery("deviceInfo { name model appVersion appBuildNumber kernelVersion totalMemory display { width height density } }").getValue("deviceInfo").jsonObject
        assertTrue(data.getValue("model").jsonPrimitive.content.isNotEmpty())
        assertTrue(data.getValue("appVersion").jsonPrimitive.content.isNotEmpty())
        assertTrue(data.getValue("kernelVersion").jsonPrimitive.content.isNotEmpty())
        assertTrue(data.getValue("totalMemory").jsonPrimitive.long > 0)
        val display = data.getValue("display").jsonObject
        assertTrue(display.getValue("width").jsonPrimitive.int > 0)
        assertTrue(display.getValue("height").jsonPrimitive.int > 0)
        assertTrue(display.getValue("density").jsonPrimitive.double > 0)
    }
}

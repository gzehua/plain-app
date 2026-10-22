package com.ismartcoding.plain.tests

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.features.sessions.RustSessionStore
import com.ismartcoding.plain.platform.system.getDeviceCapabilities
import com.ismartcoding.plain.preferences.UserPrefs
import com.ismartcoding.plain.rust.graphql.ApiSession
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
            val created = RustSessionStore.create(name)
            val clientId = created.clientId
            id = clientId
            val token = created.token
            assertTrue(token.isNotEmpty())
            val app = RustGraphqlApi.query("app { httpPort urlToken capabilities }").getValue("app").jsonObject
            assertEquals(getDeviceCapabilities().map { it.name }.toSet(), app.getValue("capabilities").jsonArray.map { it.jsonPrimitive.content }.toSet())
            val remote = ApiSession("http://127.0.0.1:${app.getValue("httpPort").jsonPrimitive.int}", clientId, token)
            val schema = "__schema { queryType { name } mutationType { name } }"
            assertEquals(RustGraphqlApi.query(schema), RustGraphqlApi.query(schema, remote))
            assertEquals(app.getValue("urlToken"), RustGraphqlApi.query("app { urlToken }", remote).getValue("app").jsonObject.getValue("urlToken"))
            com.ismartcoding.plain.preferences.RustSystemState.refresh()
            assertEquals(app.getValue("urlToken").jsonPrimitive.content, com.ismartcoding.plain.preferences.RustSystemState.state.value.urlToken)
            for (selection in listOf("systemPrefs", "systemSettings { password }", "userSettings { service }")) {
                for (session in listOf(null, remote)) {
                    val failure = runCatching { RustGraphqlApi.query(selection, session) }.exceptionOrNull()
                    assertNotNull(failure)
                    assertTrue(failure.toString().contains("Unknown field"))
                }
            }
            assertTrue(runCatching { RustGraphqlApi.query("sessions { token }", remote) }.isFailure)
            assertTrue(RustGraphqlApi.query("sessions { clientId }", remote).getValue("sessions").jsonArray.any { it.jsonObject.getValue("clientId").jsonPrimitive.content == clientId })
            assertTrue(RustGraphqlApi.graphql("mutation(\$id:String!,\$name:String!){renameSession(clientId:\$id,name:\$name)}", mapOf("id" to JsonPrimitive(clientId), "name" to JsonPrimitive("$name renamed")), remote).getValue("renameSession").jsonPrimitive.boolean)
            assertEquals("$name renamed", RustSessionStore.list().single { it.clientId == clientId }.name)
        } finally {
            id?.let { RustSessionStore.delete(it) }
            UserPrefs.service.value = oldService
            UserPrefs.desktopAccess.value = oldAccess
        }
    }

    @Test
    fun selectedDeviceFieldsReadActualSdkFactsThroughTheMainNativeExecutor() = runBlocking {
        val data = RustGraphqlApi.query("deviceInfo { name model appVersion appBuildNumber kernelVersion totalMemory display { width height density } }").getValue("deviceInfo").jsonObject
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

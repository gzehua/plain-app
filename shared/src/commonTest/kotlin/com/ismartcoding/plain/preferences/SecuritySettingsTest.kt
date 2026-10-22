package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SecuritySettingsTest {
    private fun fixture(): JsonObject = Json.parseToJsonElement(
        """{
            "password":"fixture-password", "passwordType":1, "loginRequireApproval":true,
            "rotateUrlTokenOnRestart":true, "adbToken":"fixture-adb",
            "autoCheckUpdate":true, "skipVersion":"8.0.0",
            "updateInfo":{
                "newVersion":"9.0.0", "checkUpdateTime":4294967296,
                "publishDate":"2026-01-01", "log":"fixture changes",
                "downloadUrl":"", "size":1099511627776, "downloadedApkPath":""
            },
            "appUpdateAvailable":true, "urlToken":"fixture-url",
            "apiPermissions":["READ_SMS","READ_CONTACTS","READ_SMS"],
            "onboardingCompleted":true, "clientId":"fixture-client",
            "mdnsHostname":"fixture", "signaturePublicKey":"fixture-public-key"
        }""",
    ).jsonObject

    @Test
    fun canonicalModelPreservesTypedUpdateDataAndPermissionSetWireShape() {
        val input = fixture()
        val settings = JsonHelper.jsonDecodeFromElement<SecuritySettings>(input)
        assertEquals(setOf("READ_SMS", "READ_CONTACTS"), settings.apiPermissions)
        assertEquals(4294967296L, settings.updateInfo.checkUpdateTime)
        assertEquals(1099511627776L, settings.updateInfo.size)
        assertEquals("fixture changes", settings.updateInfo.log)
        assertTrue(settings.appUpdateAvailable)
        assertTrue(settings.loginRequireApproval)
        assertTrue(settings.rotateUrlTokenOnRestart)
        val output = JsonHelper.jsonEncodeToElement(settings).jsonObject
        assertEquals(input["updateInfo"], output["updateInfo"])
        assertEquals(settings.apiPermissions, output.getValue("apiPermissions").jsonArray.map { it.jsonPrimitive.content }.toSet())
        for ((name, value) in input.filterKeys { it != "apiPermissions" }) {
            assertEquals(value, output[name], name)
        }
    }

    @Test
    fun missingAvailabilityKeepsTheRuntimeDefault() {
        val input = JsonObject(fixture().filterKeys { it != "appUpdateAvailable" })
        val settings = JsonHelper.jsonDecodeFromElement<SecuritySettings>(input)
        assertFalse(settings.appUpdateAvailable)
        assertTrue(settings.autoCheckUpdate)
        assertEquals("fixture-client", settings.clientId)
    }
}

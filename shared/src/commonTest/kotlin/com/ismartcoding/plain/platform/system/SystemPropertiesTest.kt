package com.ismartcoding.plain.platform.system

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlinx.serialization.json.JsonPrimitive

class SystemPropertiesTest {
    @Test
    fun startupAndDisplayReadsShareOnePlatformSample() {
        for ((extra, expectedOs) in listOf(
            mapOf("android.sdkVersion" to JsonPrimitive(35)) to "Android 15 (API 35)",
            mapOf("osName" to JsonPrimitive("iOS")) to "iOS 15",
        )) {
            var reads = 0
            val properties = StartupSystemProperties {
                reads++
                mapOf(
                    "name" to JsonPrimitive("Device"),
                    "osVersion" to JsonPrimitive("15"),
                    "appVersion" to JsonPrimitive("3.3.25"),
                    "appBuildNumber" to JsonPrimitive("123"),
                ) + extra
            }
            assertEquals(0, reads)
            repeat(3) {
                assertEquals("3.3.25 (123)", properties.appVersion())
                assertEquals("3.3.25", properties.versionName())
                assertEquals("Device", properties.deviceName())
                assertEquals(123L, properties.versionCode())
                assertEquals(expectedOs, properties.osVersion())
                assertSame(properties.snapshot(), properties.snapshot())
            }
            assertEquals(1, reads)
        }
    }

    @Test
    fun missingBuildNumberKeepsTheVersionDisplayAndZeroCode() {
        val properties = StartupSystemProperties { mapOf("appVersion" to JsonPrimitive("3.3.25")) }
        assertEquals("3.3.25", properties.appVersion())
        assertEquals(0L, properties.versionCode())
    }

    @Test
    fun fixedAndUnknownPropertiesCannotReachTheRuntimeSampler() {
        for (name in listOf(
            "dataDir", "systemDbPath", "cacheDir", "downloadsDir", "name", "model",
            "osVersion", "appVersion", "appBuildNumber", "wifiAwareSupported",
            "media.systemCatalog",
            "android.sdkVersion", "permissions", "unknown",
        )) {
            assertFailsWith<IllegalArgumentException>(name) {
                sampleSystemProperties(listOf("language", name))
            }
        }
    }

    @Test
    fun emptySelectionDoesNotNeedAnInitializedPlatform() {
        assertEquals(emptyMap(), sampleSystemProperties(emptyList()))
    }
}

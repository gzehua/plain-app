package com.ismartcoding.plain.preferences

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserSettingsClientTest {
    @Test
    fun readsOneExistingMainSchemaSnapshotAndPreservesJsonValues() = runTest {
        val expected = Json.parseToJsonElement("""{"httpPort":8080,"deviceName":"Fixture","feedAutoRefreshIntervalSec":2345,"recentSearches":["second","first"],"notificationFilter":{"mode":"allowlist","apps":[]}}""") as JsonObject
        var requests = 0
        val result = readUserSettings { selection ->
            requests++
            assertEquals(USER_SETTINGS_SELECTION, selection)
            JsonObject(mapOf("userSettings" to expected))
        }
        assertEquals(expected, result)
        assertEquals(1, requests)
    }

    @Test
    fun propagatesFailureWithoutRetryOrFallingBackToANativeSnapshot() = runTest {
        var requests = 0
        assertFailsWith<IllegalStateException> {
            readUserSettings { requests++; error("read failed") }
        }
        assertEquals(1, requests)
        assertFailsWith<NoSuchElementException> { readUserSettings { JsonObject(emptyMap()) } }
    }
}

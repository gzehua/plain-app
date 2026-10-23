package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PrefKeyTest {
    @Test
    fun preferencesUseRustStorageKeysAndReadTheirSnapshotFields() = runTest {
        Prefs.applySnapshot(JsonObject(SecuritySettingsTest().fixture() + JsonHelper.jsonDecode<JsonObject>(
            """{"httpPort":8181,"httpsPort":8543,"backgroundServiceEnabled":false,"feedAutoRefreshIntervalSec":900,"packageSortBy":4}"""
        )))
        for ((pref, key, field) in listOf(
            Triple(Prefs.httpPort, "http_port", "httpPort"),
            Triple(Prefs.backgroundServiceEnabled, "background_service_enabled", "backgroundServiceEnabled"),
            Triple(Prefs.feedAutoRefreshInterval, "feed_auto_refresh_interval", "feedAutoRefreshIntervalSec"),
            Triple(Prefs.packageSortBy, "pkg_sort_by", "packageSortBy"),
        )) {
            assertEquals(key, pref.key)
            assertEquals(field, prefSnapshotKey(pref.key))
        }
    }

    @Test
    fun hostUpdatesOnlyTheRequestedValuesWithoutWritingBackToRust() = runTest {
        Prefs.applySnapshot(JsonObject(SecuritySettingsTest().fixture() + JsonHelper.jsonDecode<JsonObject>(
            """{"httpPort":8181,"httpsPort":8543,"backgroundServiceEnabled":false,"feedAutoRefreshIntervalSec":900,"packageSortBy":4}"""
        )))
        val port = Prefs.httpPort
        val https = Prefs.httpsPort.value
        Prefs.update(JsonHelper.jsonDecode("""{"httpPort":8282}"""))
        assertEquals(8282, port.value)
        assertEquals(https, Prefs.httpsPort.value)
        assertEquals(8282, JsonHelper.jsonDecodeFromElement<Int>(Prefs.initialValue(PrefKeys.HTTP_PORT)))
        Prefs.update(JsonHelper.jsonDecode("""{"httpPort":8383,"httpsPort":8643}"""))
        assertEquals(8383, port.value)
        assertEquals(8643, Prefs.httpsPort.value)
    }

    @Test
    fun unknownSnapshotKeysFailInsteadOfGuessingAFieldName() {
        assertFailsWith<IllegalStateException> { prefSnapshotKey("missing_key") }
    }
}

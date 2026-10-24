package com.ismartcoding.plain.rust.native

import com.ismartcoding.plain.models.UpdateInfo
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NativePreferenceReplyTest {
    @Test
    fun nativeResponsesDecodeDirectlyToRequestedTypes() {
        assertEquals(8181, nativeValue<Int>("""{"value":8181}"""))
        assertEquals(true, nativeValue<Boolean>("""{"value":true}"""))
        assertEquals("value", nativeValue<String>("""{"value":"value"}"""))
        assertEquals(listOf("a", "b"), nativeValue<List<String>>("""{"value":["a","b"]}"""))
    }

    @Test
    fun updateAndPermissionKeysDecodeWithoutAnAggregateSettingsModel() {
        val update = nativeValue<UpdateInfo>(
            """{"value":{"newVersion":"9.0.0","checkUpdateTime":4294967296,
                "publishDate":"2026-01-01","log":"changes","downloadUrl":"",
                "size":1099511627776,"downloadedApkPath":""}}""",
        )
        assertEquals(4294967296L, update.checkUpdateTime)
        assertEquals(1099511627776L, update.size)
        assertEquals("changes", update.log)
        assertEquals(setOf("READ_SMS"), nativeValue<Set<String>>("""{"value":["READ_SMS","READ_SMS"]}"""))
    }

    @Test
    fun errorsMissingValuesAndWrongTypesAreRejected() {
        assertFailsWith<IllegalStateException> { nativeValue<Int>("""{"error":"Unknown preference"}""") }
        assertFailsWith<IllegalArgumentException> { nativeValue<Int>("""{}""") }
        assertFailsWith<SerializationException> { nativeValue<Int>("""{"value":"invalid"}""") }
    }
}

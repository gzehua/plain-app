package com.ismartcoding.plain.features.shares

import com.ismartcoding.plain.lib.JsonHelper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class SharedInfoDtoTest {
    @Test
    fun mainSchemaExpiryPreservesInstantMillisecondsAndNull() {
        val row = JsonHelper.jsonDecode<SharedInfoDto>("""{"name":"Share","readOnly":true,"requiresPassword":false,"expiresAt":"2030-01-01T00:00:00.123Z"}""")
        assertEquals(Instant.parse("2030-01-01T00:00:00.123Z"), row.expiresAt)
        assertNull(JsonHelper.jsonDecode<SharedInfoDto>("""{"name":"Share","readOnly":true,"requiresPassword":false,"expiresAt":null}""").expiresAt)
    }
}

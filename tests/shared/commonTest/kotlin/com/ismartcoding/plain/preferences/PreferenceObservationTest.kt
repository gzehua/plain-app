package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.Prefs
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PreferenceObservationTest {
    @Test
    fun typedUpdatesChangeOnlyTheirOwnPreference() {
        val oldPort = Prefs.httpPort.value
        val oldHttps = Prefs.https.value
        try {
            Prefs.update(PrefKeys.HTTP_PORT, JsonPrimitive(8080))
            assertEquals(8080, Prefs.httpPort.value)
            assertEquals(oldHttps, Prefs.https.value)
            assertFailsWith<IllegalArgumentException> { Prefs.update(PrefKeys.HTTP_PORT, JsonPrimitive("invalid")) }
            assertEquals(8080, Prefs.httpPort.value)
        } finally { Prefs.httpPort.value = oldPort }
    }
}

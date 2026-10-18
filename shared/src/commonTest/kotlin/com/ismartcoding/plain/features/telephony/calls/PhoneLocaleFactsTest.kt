package com.ismartcoding.plain.features.telephony.calls

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PhoneLocaleFactsTest {
    @Test
    fun localeFactsKeepRawRegionsForRustToSelect() {
        val facts = PhoneLocaleFacts("US", "en-US", "cn", "gb")
        val encoded = JsonHelper.jsonEncodeToElement(facts)
        assertEquals("US", encoded.jsonObject.getValue("region").jsonPrimitive.content)
        assertEquals("cn", encoded.jsonObject.getValue("networkRegion").jsonPrimitive.content)
        assertEquals("gb", encoded.jsonObject.getValue("simRegion").jsonPrimitive.content)
        assertEquals("en-US", encoded.jsonObject.getValue("locale").jsonPrimitive.content)
        assertFalse("available" in encoded.jsonObject)
        assertEquals(facts, JsonHelper.jsonDecodeFromElement<PhoneLocaleFacts>(encoded))
    }
}

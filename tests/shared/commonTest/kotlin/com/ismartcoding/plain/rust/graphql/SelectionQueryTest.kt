package com.ismartcoding.plain.rust.graphql

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SelectionQueryTest {
    @Test
    fun preservesOrderAndExactIdsAcrossBothQueryEncodingLayers() {
        val ids = listOf(" second ", "=opaque", "second", " second ")
        val expected = "ids:=\\ second\\ ,=opaque,second"
        assertEquals(expected, selectionQuery(ids))
        assertEquals(expected, Json.parseToJsonElement(JsonHelper.jsonEncode(selectionQuery(ids))).jsonPrimitive.content)
    }

    @Test
    fun quotesAndFilterSyntaxStayInsideTheSelectedIds() {
        assertEquals("ids:=one\\ peer:other", selectionQuery(listOf("one peer:other")))
        assertEquals("ids:=one\\\nall:true", selectionQuery(listOf("one\nall:true")))
        assertEquals("ids:=\\\"quoted\\\"\\\\path", selectionQuery(listOf("\"quoted\"\\path")))
        assertEquals("ids:=媒体：42", selectionQuery(listOf("媒体：42")))
    }

    @Test
    fun rejectsEmptyBlankAndAmbiguousCsvSelections() {
        assertFailsWith<IllegalArgumentException> { selectionQuery(emptyList()) }
        for (id in listOf("", " \t\n", "one,two")) {
            assertFailsWith<IllegalArgumentException>(id) { selectionQuery(listOf(id)) }
        }
    }
}

package com.ismartcoding.plain.features.media

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MediaSelectionClientTest {
    @Test
    fun sharing_resolves_the_entire_selection_including_items_not_loaded_by_the_page() = runTest {
        var calls = 0
        val paths = readSelectedMediaPaths("images", listOf("loaded", "not-loaded", "last")) { document ->
            calls += 1
            assertEquals("ids:=loaded,not-loaded,last", document.variables.getValue("query").jsonPrimitive.content)
            assertEquals(3, document.variables.getValue("limit").jsonPrimitive.int)
            Json.parseToJsonElement("""{"images":[{"id":"last","path":"/photos/last.jpg"},{"id":"not-loaded","path":"/photos/not loaded.jpg"},{"id":"loaded","path":"/photos/loaded.jpg"}]}""").jsonObject
        }
        assertEquals(listOf("loaded" to "/photos/loaded.jpg", "not-loaded" to "/photos/not loaded.jpg", "last" to "/photos/last.jpg"), paths)
        assertEquals(1, calls)
    }

    @Test
    fun empty_selection_never_queries_the_library_and_missing_items_do_not_silently_share_a_subset() = runTest {
        assertEquals(emptyList(), readSelectedMediaPaths("images", emptyList()) { error("No query expected") })
        assertFailsWith<NoSuchElementException> {
            readSelectedMediaPaths("images", listOf("loaded", "deleted")) {
                Json.parseToJsonElement("""{"images":[{"id":"loaded","path":"/photos/loaded.jpg"}]}""").jsonObject
            }
        }
    }
}

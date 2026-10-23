package com.ismartcoding.plain.features.notes

import kotlinx.serialization.json.*
import com.ismartcoding.plain.rust.graphql.GraphqlRequest
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotesApiTest {
    private val note = JsonHelper.jsonDecode<JsonObject>("""{"id":"server-note","title":"Title","content":"Body","deletedAt":null,"createdAt":"2026-01-01T00:00:00Z","updatedAt":"2026-01-01T00:00:00Z"}""")

    @Test
    fun create_and_edit_submit_only_the_edit_values_without_reading_a_record() = runTest {
        val requests = mutableListOf<GraphqlRequest>()
        val api = NotesApi(query = { error("Unexpected preread: $it") }, mutate = {
            requests.add(it)
            JsonObject(mapOf((if (it.query.contains("createNote(")) "createNote" else "updateNote") to note))
        })
        assertEquals("server-note", api.save("", "Title", "Body").id)
        assertEquals(1, requests.size)
        assertTrue(requests.single().query.contains("createNote("))
        assertFalse("id" in requests.single().variables)
        requests.clear()
        assertEquals("server-note", api.save("server-note", "", "Line\n\"quoted\"").id)
        assertEquals(1, requests.size)
        assertTrue(requests.single().query.contains("updateNote("))
        assertEquals(JsonPrimitive("server-note"), requests.single().variables["id"])
        assertEquals(JsonPrimitive(""), requests.single().variables.getValue("input").jsonObject["title"])
        assertEquals(JsonPrimitive("Line\n\"quoted\""), requests.single().variables.getValue("input").jsonObject["content"])
        assertFalse("createdAt" in requests.single().variables)
    }

    @Test
    fun a_failed_save_propagates_the_failure_without_retry_or_fake_local_result() = runTest {
        var writes = 0
        val api = NotesApi(query = { error("Unexpected preread") }, mutate = {
            writes += 1
            error("Note missing")
        })
        val failure = assertFailsWith<IllegalStateException> { api.save("missing", "Title", "Body") }
        assertEquals("Note missing", failure.message)
        assertEquals(1, writes)
    }
}

package com.ismartcoding.plain.features.tags

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.system.DataType
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TagsApiTest {
    @Test
    fun create_and_rename_do_not_preread_or_generate_local_records() = runTest {
        val requests = mutableListOf<String>()
        val api = TagsApi(query = { error("Unexpected preread: $it") }, mutate = {
            requests.add(it)
            val field = if (it.startsWith("createTag")) "createTag" else "updateTag"
            JsonHelper.jsonDecode<JsonObject>("""{"$field":{"id":"server-tag","name":"Label","count":0,"type":6}}""")
        })
        assertEquals("server-tag", api.create(DataType.NOTE, "Label").id.also { assertEquals(1, requests.size) })
        assertTrue(requests.single().startsWith("createTag(type: NOTE,"))
        assertFalse(requests.single().contains("id:"))
        requests.clear()
        assertEquals("server-tag", api.rename("server-tag", "New\n\"label\"").id)
        assertEquals(1, requests.size)
        assertTrue(requests.single().startsWith("updateTag(id: \"server-tag\","))
        assertTrue(requests.single().contains("name: \"New\\n\\\"label\\\"\""))
        assertFalse(requests.single().contains("type:"))
    }
    @Test
    fun batch_edits_send_one_key_list_without_metadata_or_prereads() = runTest {
        val requests = mutableListOf<String>()
        val api = TagsApi(query = { error("Unexpected preread") }, mutate = {
            requests.add(it)
            JsonHelper.jsonDecode<JsonObject>("""{"updateTagRelations":true}""")
        })
        api.edit(DataType.FILE, listOf("/a, b", "/second"), listOf("add"), listOf("remove"))
        assertEquals(1, requests.size)
        assertTrue(requests.single().contains("keys: [\"/a, b\",\"/second\"]"))
        assertFalse(requests.single().contains("title:"))
        assertFalse(requests.single().contains("size:"))
        assertFalse(requests.single().contains("sizeBytes:"))
        requests.clear()
        api.edit(DataType.FILE, emptyList(), listOf("add"), emptyList())
        api.edit(DataType.FILE, listOf("/a"), emptyList(), emptyList())
        assertTrue(requests.isEmpty())
    }
}

package com.ismartcoding.plain.features.feeds

import kotlinx.serialization.json.*
import com.ismartcoding.plain.rust.graphql.GraphqlRequest
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeedsApiTest {
    private val feed = JsonHelper.jsonDecode<JsonObject>("""{"id":"fixture","name":"Named","url":"https://example.org/feed","fetchContent":false,"logo":"","lastSyncAt":null,"lastError":{"code":"","detail":""},"createdAt":"2026-01-01T00:00:00Z","updatedAt":"2026-01-01T00:00:00Z"}""")

    @Test
    fun create_and_edit_each_make_one_mutation_without_prereads_or_second_steps() = runTest {
        val requests = mutableListOf<GraphqlRequest>()
        val api = FeedsApi(query = { error("Unexpected preread: $it") }, mutate = {
            requests.add(it)
            JsonObject(mapOf((if (it.query.contains("createFeed(")) "createFeed" else "updateFeed") to feed))
        })
        assertEquals("fixture", api.create("https://example.org/feed", "Named").id)
        assertEquals(1, requests.size)
        assertEquals(JsonPrimitive("Named"), requests.single().variables["name"])
        assertFalse(requests.single().query.contains("syncFeeds"))
        requests.clear()
        api.update("fixture", name = "Renamed", url = "https://example.org/new", fetchContent = false)
        assertEquals(1, requests.size)
        assertTrue(requests.single().query.contains("updateFeed("))
        assertFalse(requests.single().query.contains("updateFeedUrl"))
        requests.clear()
        api.update("fixture", fetchContent = true)
        assertEquals(1, requests.size)
        assertEquals(JsonPrimitive(true), requests.single().variables["fetchContent"])
        assertFalse("name" in requests.single().variables)
        assertFalse("url" in requests.single().variables)
    }

    @Test
    fun preview_is_one_query_without_mutations_and_decodes_the_contract_dto() = runTest {
        val requests = mutableListOf<GraphqlRequest>()
        val api = FeedsApi(query = {
            requests.add(it)
            JsonHelper.jsonDecode<JsonObject>("""{"feedPreview":{"title":"A & B"}}""")
        }, mutate = { error("Preview must not persist: $it") })
        assertEquals("A & B", api.preview("https://example.org/feed").title)
        assertEquals(1, requests.size)
        assertTrue(requests.single().query.contains("feedPreview("))
    }
    @Test
    fun list_and_counts_share_one_request_and_project_the_returned_counts() = runTest {
        val requests = mutableListOf<GraphqlRequest>()
        val api = FeedsApi(query = {
            requests.add(it)
            JsonHelper.jsonDecode<JsonObject>("""{"feeds":[$feed],"feedEntryCounts":[{"id":"fixture","count":7}]}""")
        }, mutate = { error("List must not mutate") })
        assertEquals(7, api.getAll(withCount = true).single().entryCount)
        assertEquals(1, requests.size)
        assertTrue(requests.single().query.contains("feedEntryCounts"))
    }

}

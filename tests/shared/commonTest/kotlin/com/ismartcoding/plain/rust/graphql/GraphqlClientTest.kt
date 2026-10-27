package com.ismartcoding.plain.rust.graphql

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.models.ChatItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class GraphqlClientTest {
    @Test
    fun one_execution_selects_and_decodes_the_request_result() = runTest {
        val request = Query.audioQueueItems(0, 10, "")
        var calls = 0
        val api = GraphqlClient { received ->
            calls++
            assertSame(request, received)
            JsonHelper.jsonDecode<GraphqlResponse>(
                """{"data":{"audioQueueItems":[{"mediaId":"track","title":"Song","artist":"Artist","path":"/music/song.mp3","durationMs":3000000123}],"unrelated":null},"errors":[]}""",
            )
        }

        val items = api.execute<List<AudioItem>>(request)
        assertEquals(listOf(AudioItem("track", "Song", "Artist", "/music/song.mp3", 3_000_000_123)), items)
        assertEquals(1, calls)
    }

    @Test
    fun unit_execution_checks_errors_without_decoding_an_unused_value() = runTest {
        val request = Mutation.startPomodoro(60)
        var calls = 0
        val api = GraphqlClient {
            calls++
            GraphqlResponse(data = JsonObject(emptyMap()))
        }
        assertEquals(Unit, api.execute<Unit>(request))
        assertEquals(1, calls)

        val failed = GraphqlClient {
            GraphqlResponse(data = JsonObject(emptyMap()), errors = listOf(GraphqlError("no_permission")))
        }
        assertEquals("no_permission", assertFailsWith<IllegalStateException> {
            failed.execute<Unit>(request)
        }.message)
    }

    @Test
    fun composite_requests_decode_the_complete_response() = runTest {
        val api = GraphqlClient {
            JsonHelper.jsonDecode<GraphqlResponse>("""{"data":{"message0":null}}""")
        }
        val snapshot = api.execute<Map<String, ChatItem?>>(Query.chatSnapshot(listOf("missing")))
        assertEquals(mapOf("message0" to null), snapshot)
    }

    @Test
    fun null_results_missing_fields_and_invalid_rows_remain_distinct() = runTest {
        val request = Query.chatItem("missing")
        val nullable = GraphqlClient {
            JsonHelper.jsonDecode<GraphqlResponse>("""{"data":{"chatItem":null}}""")
        }
        assertNull(nullable.execute<ChatItem?>(request))

        val missing = GraphqlClient { GraphqlResponse(data = JsonObject(emptyMap())) }
        assertFailsWith<NoSuchElementException> { missing.execute<ChatItem?>(request) }

        val invalid = GraphqlClient {
            JsonHelper.jsonDecode<GraphqlResponse>("""{"data":{"audioQueueItems":[{}]}}""")
        }
        assertFailsWith<SerializationException> {
            invalid.execute<List<AudioItem>>(Query.audioQueueItems(0, 10, ""))
        }
        val noData = GraphqlClient { GraphqlResponse() }
        assertFailsWith<IllegalStateException> { noData.execute<Unit>(Mutation.startPomodoro(60)) }
    }

    @Test
    fun request_failures_and_cancellation_propagate_without_retries() = runTest {
        val request = Query.audioQueueItemCount()
        var calls = 0
        val failure = IllegalStateException("Request failed")
        val failed = GraphqlClient { calls++; throw failure }
        assertSame(failure, assertFailsWith<IllegalStateException> { failed.execute<Int>(request) })
        assertEquals(1, calls)

        val cancellation = CancellationException("Request cancelled")
        val cancelled = GraphqlClient { calls++; throw cancellation }
        assertSame(cancellation, assertFailsWith<CancellationException> { cancelled.execute<Int>(request) })
        assertEquals(2, calls)
    }
}

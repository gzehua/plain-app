package com.ismartcoding.plain.rust.graphql

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

class RustGraphqlReadTest {
    @Test
    fun request_metadata_selects_and_decodes_the_complete_list_once() = runTest {
        val request = Query.audioQueueItems(0, 10, "")
        var calls = 0
        val items = RustGraphqlApi.read<List<AudioItem>>(request) { received ->
            calls++
            assertSame(request, received)
            JsonHelper.jsonDecode<JsonObject>(
                """{"audioQueueItems":[{"mediaId":"track","title":"Song","artist":"Artist","path":"/music/song.mp3","durationMs":3000000123}],"unrelated":null}""",
            )
        }

        assertEquals(listOf(AudioItem("track", "Song", "Artist", "/music/song.mp3", 3_000_000_123)), items)
        assertEquals(1, calls)
    }

    @Test
    fun null_results_missing_fields_and_invalid_rows_remain_distinct() = runTest {
        val request = Query.chatItem("missing")
        val item = RustGraphqlApi.read<ChatItem?>(request) {
            JsonHelper.jsonDecode<JsonObject>("""{"chatItem":null}""")
        }
        assertNull(item)

        assertFailsWith<NoSuchElementException> {
            RustGraphqlApi.read<ChatItem?>(request) { JsonObject(emptyMap()) }
        }
        assertFailsWith<SerializationException> {
            RustGraphqlApi.read<List<AudioItem>>(Query.audioQueueItems(0, 10, "")) {
                JsonHelper.jsonDecode<JsonObject>("""{"audioQueueItems":[{}]}""")
            }
        }
    }

    @Test
    fun request_failures_and_cancellation_propagate_without_substitution() = runTest {
        val request = Query.audioQueueItemCount()
        val failure = IllegalStateException("Request failed")
        assertSame(failure, assertFailsWith<IllegalStateException> {
            RustGraphqlApi.read<Int>(request) { throw failure }
        })
        val cancellation = CancellationException("Request cancelled")
        assertSame(cancellation, assertFailsWith<CancellationException> {
            RustGraphqlApi.read<Int>(request) { throw cancellation }
        })
    }
}

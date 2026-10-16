package com.ismartcoding.plain.rust.request

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.*
import kotlin.test.*

class RustUploadChunksTest {
    private class Fixture {
        val chunks = mutableListOf<ByteArray>()
        val indices = mutableListOf<Long>()
        val ids = mutableSetOf<String>()
        val mutations = mutableListOf<String>()
        var failure = ""
        var queries = 0
        var mergeStatus = "DONE"
        var queryError: Exception? = null
        val client = UploadChunksClient(
            sendChunk = { id, index, bytes -> ids.add(id); indices.add(index); chunks.add(bytes.copyOf()); failure },
            mutate = { body ->
                mutations.add(body)
                when {
                    body.startsWith("mergeChunks") -> buildJsonObject { putJsonObject("mergeChunks") { put("status", "STARTED") } }
                    else -> buildJsonObject { put("deleteChunks", true) }
                }
            },
            query = { queries++; queryError?.let { throw it }; buildJsonObject { putJsonObject("mergeStatus") { put("status", mergeStatus); put("error", "Fixture merge failure") } } },
        )
    }

    @Test fun save_batches_small_chunks_and_preserves_raw_bytes() = runTest {
        val fixture = Fixture()
        val bytes = ByteArray(1024 * 1024 * 2 + 3) { (it % 256).toByte() }
        val input = bytes.asSequence().chunked(17).map { it.toByteArray() }.iterator()
        assertTrue(fixture.client.replace("/fixture/editor.bin", input))
        assertEquals(listOf(1024 * 1024, 1024 * 1024, 3), fixture.chunks.map { it.size })
        assertContentEquals(bytes, fixture.chunks.fold(ByteArray(0)) { total, chunk -> total + chunk })
        assertEquals(listOf(0L, 1L, 2L), fixture.indices)
        assertEquals(1, fixture.ids.size)
        assertTrue(fixture.mutations.first().contains("totalChunks: 3"))
        assertTrue(fixture.mutations.first().contains("totalSize: ${bytes.size}"))
        assertTrue(fixture.mutations.first().contains("replace: true"))
        assertEquals(1, fixture.queries)
        assertTrue(fixture.mutations.last().startsWith("deleteChunks"))
    }

    @Test fun empty_document_still_commits_an_empty_file() = runTest {
        val fixture = Fixture()
        assertTrue(fixture.client.replace("/fixture/empty.txt", emptyList<ByteArray>().iterator()))
        assertEquals(1, fixture.chunks.size)
        assertTrue(fixture.chunks.single().isEmpty())
        assertTrue(fixture.mutations.first().contains("totalSize: 0"))
    }

    @Test fun failed_upload_never_starts_a_merge_and_removes_staged_chunks() = runTest {
        val fixture = Fixture()
        fixture.failure = "Fixture write failure"
        assertFailsWith<IllegalStateException> { fixture.client.replace("/fixture/unchanged.bin", listOf(byteArrayOf(1)).iterator()) }
        assertEquals(1, fixture.mutations.size)
        assertTrue(fixture.mutations.single().startsWith("deleteChunks"))
        assertEquals(0, fixture.queries)
    }

    @Test fun producer_cancellation_preserves_cancellation_and_cleans_staging() = runTest {
        val fixture = Fixture()
        val chunks = object : Iterator<ByteArray> {
            override fun hasNext(): Boolean = throw CancellationException("Fixture cancellation")
            override fun next(): ByteArray = error("No bytes")
        }
        assertFailsWith<CancellationException> { fixture.client.replace("/fixture/unchanged.bin", chunks) }
        assertTrue(fixture.chunks.isEmpty())
        assertTrue(fixture.mutations.single().startsWith("deleteChunks"))
    }
    @Test fun failed_merge_reports_failure_and_cleans_staging() = runTest {
        val fixture = Fixture()
        fixture.mergeStatus = "FAILED"
        val error = assertFailsWith<IllegalStateException> {
            fixture.client.replace("/fixture/unchanged.bin", listOf(byteArrayOf(0, -1)).iterator())
        }
        assertEquals("Fixture merge failure", error.message)
        assertTrue(fixture.mutations.first().startsWith("mergeChunks"))
        assertTrue(fixture.mutations.last().startsWith("deleteChunks"))
    }

    @Test fun cancellation_while_waiting_for_merge_also_deletes_chunks() = runTest {
        val fixture = Fixture()
        fixture.queryError = CancellationException("Fixture merge cancellation")
        assertFailsWith<CancellationException> {
            fixture.client.replace("/fixture/unchanged.bin", listOf(byteArrayOf(1)).iterator())
        }
        assertEquals(1, fixture.queries)
        assertTrue(fixture.mutations.last().startsWith("deleteChunks"))
    }

}

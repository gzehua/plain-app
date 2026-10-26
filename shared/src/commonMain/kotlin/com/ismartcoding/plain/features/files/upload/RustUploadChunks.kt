package com.ismartcoding.plain.features.files.upload

import com.ismartcoding.plain.rust.graphql.GraphqlRequest
import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable
import com.ismartcoding.plain.rust.native.RustNativeBindings
import com.ismartcoding.plain.lib.logcat.LogCat
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi

internal object RustUploadChunks {
    private val client = UploadChunksClient(
        sendChunk = { id, index, bytes -> withContext(Dispatchers.IO) { RustNativeBindings.uploadChunk(id, index, bytes) } },
        mutate = { RustGraphqlApi.graphql(it) },
        query = { RustGraphqlApi.graphql(it) },
    )
    suspend fun replace(path: String, chunks: Iterator<ByteArray>): Boolean = client.replace(path, chunks)
}

internal class UploadChunksClient(
    private val sendChunk: suspend (String, Long, ByteArray) -> String,
    private val mutate: suspend (GraphqlRequest) -> JsonObject,
    private val query: suspend (GraphqlRequest) -> JsonObject,
) {
    private val maxChunkBytes = 1024 * 1024

    @OptIn(ExperimentalUuidApi::class)
    suspend fun replace(path: String, chunks: Iterator<ByteArray>): Boolean {
        val id = Uuid.random().toString()
        var index = 0L
        var size = 0L
        try {
            suspend fun upload(bytes: ByteArray) {
                currentCoroutineContext().ensureActive()
                val error = sendChunk(id, index, bytes)
                check(error.isEmpty()) { error }
                index++
                size += bytes.size
            }
            val buffer = ByteArray(maxChunkBytes)
            var buffered = 0
            while (chunks.hasNext()) {
                val bytes = chunks.next()
                var offset = 0
                while (offset < bytes.size) {
                    currentCoroutineContext().ensureActive()
                    val length = minOf(maxChunkBytes - buffered, bytes.size - offset)
                    bytes.copyInto(buffer, buffered, offset, offset + length)
                    offset += length
                    buffered += length
                    if (buffered == maxChunkBytes) { upload(buffer); buffered = 0 }
                }
            }
            if (buffered > 0) upload(buffer.copyOf(buffered))
            if (index == 0L) upload(ByteArray(0))
            var status = mutate(Mutation.mergeChunks(id, index, path, size))
                .getValue("mergeChunks").let { JsonHelper.jsonDecodeFromElement<MergeResult>(it) }
            while (status.status in setOf("STARTED", "MERGING")) {
                delay(100)
                status = query(Query.mergeStatus(id)).getValue("mergeStatus").let { JsonHelper.jsonDecodeFromElement<MergeResult>(it) }
            }
            check(status.status == "DONE") {
                status.error ?: "Upload merge did not complete"
            }
            return true
        } finally {
            withContext(NonCancellable) {
                try { mutate(Mutation.deleteChunks(id)) }
                catch (error: Exception) { LogCat.e("Upload chunk cleanup failed", error) }
            }
        }
    }
}

@Serializable
private data class MergeResult(val status: String, val error: String? = null)

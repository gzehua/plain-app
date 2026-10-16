package com.ismartcoding.plain.rust.request

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.bridge.RustCoreBridge
import com.ismartcoding.plain.rust.bridge.RustDispatchers
import com.ismartcoding.plain.lib.logcat.LogCat
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi

internal object RustUploadChunks {
    private val client = UploadChunksClient(
        sendChunk = { id, index, bytes -> withContext(RustDispatchers.requests) { RustCoreBridge.uploadChunk(id, index, bytes) } },
        mutate = { PlainRust.mainMutate(it) },
        query = { PlainRust.mainQuery(it) },
    )
    suspend fun replace(path: String, chunks: Iterator<ByteArray>): Boolean = client.replace(path, chunks)
}

internal class UploadChunksClient(
    private val sendChunk: suspend (String, Long, ByteArray) -> String,
    private val mutate: suspend (String) -> JsonObject,
    private val query: suspend (String) -> JsonObject,
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
            var status = mutate("mergeChunks(fileId: ${gql(id)}, totalChunks: $index, path: ${gql(path)}, replace: true, totalSize: $size) { status error }")
                .getValue("mergeChunks").jsonObject
            while (status.getValue("status").jsonPrimitive.content in setOf("STARTED", "MERGING")) {
                delay(100)
                status = query("mergeStatus(fileId: ${gql(id)}) { status error }").getValue("mergeStatus").jsonObject
            }
            check(status.getValue("status").jsonPrimitive.content == "DONE") {
                status["error"]?.jsonPrimitive?.contentOrNull ?: "Upload merge did not complete"
            }
            return true
        } finally {
            withContext(NonCancellable) {
                try { mutate("deleteChunks(fileId: ${gql(id)})") }
                catch (error: Exception) { LogCat.e("Upload chunk cleanup failed", error) }
            }
        }
    }
}

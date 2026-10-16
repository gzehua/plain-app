package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.platform.IODispatcher
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*

@OptIn(ExperimentalCoroutinesApi::class)
internal class FilePathResolver(
    private val scope: CoroutineScope,
    private val fetch: suspend (List<String>) -> List<String?>,
    private val maxEntries: Int = 2048,
    private val batchSize: Int = 128,
) {
    private val lock = Mutex()
    private val cache = LinkedHashMap<String, String>()
    private val waiting = LinkedHashMap<String, CompletableDeferred<String>>()
    private val queued = LinkedHashMap<String, CompletableDeferred<String>>()
    private var scheduled = false

    init { require(maxEntries > 0 && batchSize > 0) }

    suspend fun resolve(reference: String): String {
        if (!needsResolution(reference)) return reference
        scope.coroutineContext.ensureActive()
        val result = lock.withLock {
            cache.remove(reference)?.let { cache[reference] = it; return it }
            val result = waiting[reference] ?: CompletableDeferred<String>().also {
                waiting[reference] = it
                queued[reference] = it
            }
            if (!scheduled) {
                scheduled = true
                scope.launch(start = CoroutineStart.ATOMIC) {
                    try { yield(); drain() }
                    catch (error: Exception) {
                        withContext(NonCancellable) {
                            lock.withLock {
                                scheduled = false
                                waiting.values.forEach { it.completeExceptionally(error) }
                                waiting.clear()
                                queued.clear()
                            }
                        }
                        throw error
                    }
                }
            }
            result
        }
        return result.await()
    }

    suspend fun resolveAll(references: List<String>): List<String> = coroutineScope {
        references.map { async { resolve(it) } }.awaitAll()
    }

    private suspend fun drain() {
        while (true) {
            val batch = lock.withLock {
                if (queued.isEmpty()) { scheduled = false; return }
                queued.entries.take(batchSize).map { it.key to it.value }.also { selected -> selected.forEach { queued.remove(it.first) } }
            }
            val result = runCatching {
                fetch(batch.map { it.first }).also { require(it.size == batch.size) { "Invalid resolved path count" } }
            }
            lock.withLock {
                batch.forEachIndexed { index, (reference, completion) ->
                    waiting.remove(reference)
                    result.fold(
                        onSuccess = { values ->
                            val path = values[index].orEmpty()
                            if (path.isNotEmpty()) {
                                cache[reference] = path
                                while (cache.size > maxEntries) cache.remove(cache.keys.first())
                            }
                            completion.complete(path)
                        },
                        onFailure = { completion.completeExceptionally(it) },
                    )
                }
            }
        }
    }

    companion object {
        fun needsResolution(reference: String): Boolean = reference.startsWith("fid:", true) || reference.startsWith("app://", true) || reference.startsWith("file://", true)
    }
}

object FilePaths {
    private val resolver = FilePathResolver(CoroutineScope(SupervisorJob() + IODispatcher), { references ->
        PlainRust.mainGraphql(
            "query(\$paths:[String!]!){filePaths(paths:\$paths)}",
            mapOf("paths" to JsonArray(references.map(::JsonPrimitive))),
        ).getValue("filePaths").jsonArray.map { if (it is JsonNull) null else it.jsonPrimitive.content }
    })

    suspend fun resolve(reference: String): String = resolver.resolve(reference)
    suspend fun resolveAll(references: List<String>): List<String> = resolver.resolveAll(references)
}

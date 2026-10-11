package com.ismartcoding.plain.features.file

import com.ismartcoding.plain.graphql.models.FileHostTask
import com.ismartcoding.plain.preferences.RustSystemState
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.content.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.request.*
import kotlin.time.Instant
import kotlinx.coroutines.*
import kotlinx.serialization.json.*

object FileTaskHelper {
    private const val fields = "id clientId type status title error totalBytes doneBytes totalItems doneItems createdAt updatedAt completedOps { src dst }"
    private val clientId: String get() = RustSystemState.state.value.clientId.also { check(it.isNotEmpty()) { "File task identity unavailable" } }
    suspend fun create(type: FileTaskType, ops: List<FileTaskOp>, title: String = type.name): FileHostTask {
        val inputs = ops.joinToString(",") { "{ src: ${gql(it.src)}, dst: ${gql(it.dst)}, overwrite: ${it.overwrite} }" }
        return parse(RustContentApi.mutate("createFileHostTask(clientId: ${gql(clientId)}, type: ${type.name}, title: ${gql(title)}, ops: [$inputs]) { $fields }").getValue("createFileHostTask"))
    }
    suspend fun get(id: String): FileHostTask? = RustContentApi.query("fileHostTaskRecord(clientId: ${gql(clientId)}, id: ${gql(id)}) { $fields }").getValue("fileHostTaskRecord").takeUnless { it is JsonNull }?.let(::parse)
    suspend fun wait(id: String): FileHostTask {
        while (true) {
            currentCoroutineContext().ensureActive()
            val task = checkNotNull(get(id)) { "File task unavailable" }
            if (task.status == FileTaskStatus.DONE || task.status == FileTaskStatus.ERROR) return task
            delay(100)
        }
    }
    suspend fun recover(id: String): FileHostTask {
        RustContentApi.postJsonOrThrow("files/mutate", buildJsonObject { put("action", "recover"); put("clientId", clientId); put("id", id) }, longRunning = true)
        return wait(id)
    }
    suspend fun execute(type: FileTaskType, ops: List<FileTaskOp>): FileHostTask {
        val task = wait(create(type,ops).id)
        return if (task.status == FileTaskStatus.ERROR) recover(task.id) else task
    }
    suspend fun remove(id: String): Boolean = RustContentApi.mutate("removeFileHostTask(clientId: ${gql(clientId)}, id: ${gql(id)})").getValue("removeFileHostTask").jsonPrimitive.boolean
    suspend fun list(offset: Int, limit: Int, query: String): List<FileHostTask> = RustContentApi.query("fileHostTasks(clientId: ${gql(clientId)}, offset: $offset, limit: $limit, query: ${gql(query)}) { $fields }").getValue("fileHostTasks").jsonArray.map(::parse)
    private fun parse(value: JsonElement): FileHostTask = com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement(value)
}

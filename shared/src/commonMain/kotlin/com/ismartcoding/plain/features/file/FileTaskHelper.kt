package com.ismartcoding.plain.features.file

import com.ismartcoding.plain.graphql.models.FileHostTask
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.rust.request.RustContentApi
import kotlinx.coroutines.*
import kotlinx.serialization.json.*

object FileTaskHelper {
    private const val fields = "id clientId type status title error totalBytes doneBytes totalItems doneItems createdAt updatedAt completedOps { src dst }"
    suspend fun create(type: FileTaskType, ops: List<FileTaskOp>, title: String = type.name): FileHostTask {
        val inputs = ops.joinToString(",") { "{ src: ${gql(it.src)}, dst: ${gql(it.dst)}, overwrite: ${it.overwrite} }" }
        return parse(RustContentApi.mutate("createFileTask(type: ${type.name}, title: ${gql(title)}, ops: [$inputs]) { $fields }").getValue("createFileTask"))
    }
    suspend fun get(id: String): FileHostTask? = RustContentApi.query("fileTask(id: ${gql(id)}) { $fields }").getValue("fileTask").takeUnless { it is JsonNull }?.let(::parse)
    suspend fun wait(id: String): FileHostTask {
        while (true) {
            currentCoroutineContext().ensureActive()
            val task = checkNotNull(get(id)) { "File task unavailable" }
            if (task.status == FileTaskStatus.DONE || task.status == FileTaskStatus.ERROR) return task
            delay(100)
        }
    }
    suspend fun execute(type: FileTaskType, ops: List<FileTaskOp>): FileHostTask = wait(create(type, ops).id)
    suspend fun remove(id: String): Boolean = RustContentApi.mutate("removeFileTask(id: ${gql(id)})").getValue("removeFileTask").jsonPrimitive.boolean
    suspend fun list(offset: Int, limit: Int, query: String): List<FileHostTask> = RustContentApi.query("fileTasks(offset: $offset, limit: $limit, query: ${gql(query)}) { $fields }").getValue("fileTasks").jsonArray.map(::parse)
    private fun parse(value: JsonElement): FileHostTask = com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement(value)
}

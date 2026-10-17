package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.api.graphql.models.FileTask
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import kotlinx.serialization.json.*

object FileTaskClient {
    private val json = Json { encodeDefaults = true }
    private const val fields = "id clientId type status title error totalBytes doneBytes totalItems doneItems createdAt updatedAt completedOps { src dst }"

    suspend fun create(type: FileTaskType, ops: List<FileTaskOp>, title: String = type.name): FileTask = submit(type, ops, title, false)
    suspend fun execute(type: FileTaskType, ops: List<FileTaskOp>): FileTask = submit(type, ops, type.name, true)

    private suspend fun submit(type: FileTaskType, ops: List<FileTaskOp>, title: String, wait: Boolean): FileTask =
        parse(PlainRust.mainGraphql(
            "mutation(\$type:FileTaskType!,\$title:String!,\$ops:[FileTaskOpInput!]!,\$wait:Boolean!){createFileTask(type:\$type,title:\$title,ops:\$ops,waitForCompletion:\$wait){ $fields }}",
            mapOf("type" to JsonPrimitive(type.name), "title" to JsonPrimitive(title), "ops" to json.encodeToJsonElement(ops), "wait" to JsonPrimitive(wait)),
            longRunning = wait,
        ).getValue("createFileTask"))

    suspend fun get(id: String): FileTask? = PlainRust.mainQuery("fileTask(id: ${gql(id)}) { $fields }").getValue("fileTask").takeUnless { it is JsonNull }?.let(::parse)
    suspend fun remove(id: String): Boolean = PlainRust.mainMutate("removeFileTask(id: ${gql(id)})").getValue("removeFileTask").jsonPrimitive.boolean
    suspend fun list(offset: Int, limit: Int, query: String): List<FileTask> = PlainRust.mainQuery("fileTasks(offset: $offset, limit: $limit, query: ${gql(query)}) { $fields }").getValue("fileTasks").jsonArray.map(::parse)
    private fun parse(value: JsonElement): FileTask = JsonHelper.jsonDecodeFromElement(value)
}

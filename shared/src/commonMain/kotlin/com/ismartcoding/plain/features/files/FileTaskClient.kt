package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.models.FileTask
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

object FileTaskClient {
    private val json = Json { encodeDefaults = true }
    private const val fields = "id clientId type status title error totalBytes doneBytes totalItems doneItems createdAt updatedAt completedOps { src dst }"

    suspend fun execute(type: FileTaskType, ops: List<FileTaskOp>): FileTask =
        parse(RustGraphqlApi.graphql(
            "mutation(\$type:FileTaskType!,\$title:String!,\$ops:[FileTaskOpInput!]!){createFileTask(type:\$type,title:\$title,ops:\$ops,waitForCompletion:true){ $fields }}",
            mapOf("type" to JsonPrimitive(type.name), "title" to JsonPrimitive(type.name), "ops" to json.encodeToJsonElement(ops)),
        ).getValue("createFileTask"))

    private fun parse(value: JsonElement): FileTask = JsonHelper.jsonDecodeFromElement(value)
}

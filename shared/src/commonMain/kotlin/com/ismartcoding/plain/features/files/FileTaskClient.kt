package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.models.FileTask
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.PlainRust
import kotlinx.serialization.json.*

object FileTaskClient {
    private val json = Json { encodeDefaults = true }
    private const val fields = "id clientId type status title error totalBytes doneBytes totalItems doneItems createdAt updatedAt completedOps { src dst }"

    suspend fun execute(type: FileTaskType, ops: List<FileTaskOp>): FileTask =
        parse(PlainRust.mainGraphql(
            "mutation(\$type:FileTaskType!,\$title:String!,\$ops:[FileTaskOpInput!]!){createFileTask(type:\$type,title:\$title,ops:\$ops,waitForCompletion:true){ $fields }}",
            mapOf("type" to JsonPrimitive(type.name), "title" to JsonPrimitive(type.name), "ops" to json.encodeToJsonElement(ops)),
            longRunning = true,
        ).getValue("createFileTask"))

    private fun parse(value: JsonElement): FileTask = JsonHelper.jsonDecodeFromElement(value)
}

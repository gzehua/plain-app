package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.FileTask
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

object FileTaskClient {
    suspend fun execute(type: FileTaskType, ops: List<FileTaskOp>): FileTask =
        parse(RustGraphqlApi.graphql(
            Mutation.createFileTask(type, ops),
        ).getValue("createFileTask"))

    private fun parse(value: JsonElement): FileTask = JsonHelper.jsonDecodeFromElement(value)
}

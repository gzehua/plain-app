package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.platform.files.queryFileTaskMedia
import com.ismartcoding.plain.platform.files.scanFileTaskPaths
import kotlinx.serialization.json.*

object FileTaskHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = withIO {
        val paths = params.getValue("paths").jsonArray.map { it.jsonPrimitive.content }
        when (method) {
            "fileTaskScan" -> {
                scanFileTaskPaths(paths)
                JsonPrimitive(true)
            }
            "fileTaskMediaSnapshot" -> JsonHelper.jsonEncodeToElement(queryFileTaskMedia(paths))
            else -> error("Unknown file host method")
        }
    }
}

package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.rust.host.PathsRequest

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.platform.files.queryFileTaskMedia
import com.ismartcoding.plain.platform.files.scanFileTaskPaths
import kotlinx.serialization.json.*

object FileTaskHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = withIO {
        val paths = JsonHelper.jsonDecodeFromElement<PathsRequest>(params).paths
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

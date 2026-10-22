package com.ismartcoding.plain.rust.host.files

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.platform.files.queryFileTaskMedia
import com.ismartcoding.plain.platform.files.scanFileTaskPaths
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.host.PathsRequest
import kotlinx.serialization.json.*

object FileTaskHost {
    internal suspend fun handle(method: String, params: JsonObject): HostResult = withIO {
        val paths = JsonHelper.jsonDecodeFromElement<PathsRequest>(params).paths
        when (method) {
            "fileTaskScan" -> {
                scanFileTaskPaths(paths)
                hostResult(true)
            }
            "fileTaskMediaSnapshot" -> hostResult(queryFileTaskMedia(paths))
            else -> error("Unknown file host method")
        }
    }
}

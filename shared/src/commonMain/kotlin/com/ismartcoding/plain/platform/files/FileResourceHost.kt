package com.ismartcoding.plain.platform.files

import com.ismartcoding.plain.platform.getContentTypeForPath

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object FileResourceHost {
    suspend fun handle(params: JsonObject): JsonElement {
        val request = JsonHelper.jsonDecodeFromElement<FileResourceRequest>(params)
        val path = request.path
        val extra = request.params
        val operation = request.operation
        return when (operation) {
            "write" -> JsonPrimitive(writeResourceFile(extra.source, path))
            "mime" -> JsonHelper.jsonEncodeToElement(getContentTypeForPath(path))
            "packageIcon", "decodePng" -> {
                val bytes = when (operation) {
                    "packageIcon" -> getPackageIconBytes(path)
                    else -> decodeImageFileToPng(path)
                }
                if (bytes == null) JsonNull else {
                    val output = extra.output
                    ensureParentDir(output)
                    check(writeBytesToPath(output, bytes)) { "Unable to write native resource output" }
                    JsonPrimitive(true)
                }
            }
            else -> error("Unsupported native resource operation")
        }
    }

}

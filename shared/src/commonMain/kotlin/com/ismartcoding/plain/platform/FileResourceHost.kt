package com.ismartcoding.plain.platform

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object FileResourceHost {
    suspend fun handle(params: JsonObject): JsonElement {
        val request = JsonHelper.jsonDecodeFromElement<FileResourceRequest>(params)
        val path = request.path
        val extra = request.params
        val operation = request.operation
        return when (operation) {
            "mime" -> JsonHelper.jsonEncodeToElement(getContentTypeForPath(path))
            "appDir" -> JsonPrimitive(appDir())
            "probe" -> JsonPrimitive(probeVideoCodec(path))
            "transcode" -> transcodeMp4ForBrowser(path)?.let(::JsonPrimitive) ?: JsonNull
            "remux" -> remuxMp4ForBrowser(path)?.let(::JsonPrimitive) ?: JsonNull
            "convert3gp", "packageIcon", "decodePng" -> {
                val bytes = when (operation) {
                    "convert3gp" -> convert3gpToMp4(path)
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

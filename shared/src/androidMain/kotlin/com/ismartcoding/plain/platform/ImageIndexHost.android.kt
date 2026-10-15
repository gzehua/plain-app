package com.ismartcoding.plain.platform

import com.ismartcoding.plain.ai.ImageIndexDecode
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.ai.ImageIndexManager
import com.ismartcoding.plain.ai.ImageIndexCatalog
import com.ismartcoding.plain.ai.ImageSearchIndexer
import com.ismartcoding.plain.rust.content.string
import kotlinx.coroutines.*
import kotlinx.serialization.json.*

actual suspend fun handleImageIndexHost(method: String, params: JsonObject): JsonElement = withContext(Dispatchers.IO) {
    when (method) {
        "imageIndexBegin" -> ImageIndexCatalog.snapshot()
        "imageIndexPage" -> ImageIndexCatalog.page(params.string("revision"),params.string("cursor"),params.getValue("limit").jsonPrimitive.int)
        "imageIndexResolve" -> ImageIndexCatalog.resolve(params.string("revision"),params.getValue("ids").jsonArray.map { it.jsonPrimitive.content })
        "imageIndexDecode" -> JsonHelper.jsonEncodeToElement(ImageIndexDecode.decode(params.string("path")))
        "imageIndexRelease" -> JsonHelper.jsonEncodeToElement(ImageIndexDecode.release(params.string("path")))
        "imageIndexVerify" -> { ImageIndexCatalog.verify(params.string("revision"));JsonPrimitive(true) }
        "imageIndexEnd" -> JsonPrimitive(true)
        else -> error("Unknown image index host operation: $method")
    }
}
actual suspend fun disconnectImageIndexHost() = withContext(NonCancellable + Dispatchers.IO) { ImageIndexManager.shutdown() }

actual suspend fun applyImageIndexStatus(status: JsonObject) = withContext(Dispatchers.Main) { ImageSearchIndexer.applyStatus(status) }

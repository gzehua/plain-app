package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.lib.JsonHelper

import kotlinx.coroutines.*
import kotlinx.serialization.json.*

actual suspend fun handleImageCatalogHost(method: String, params: JsonObject): JsonElement = withContext(Dispatchers.IO) {
    when (method) {
        "imageCatalogSnapshot" -> MediaStoreImageCatalog.snapshot()
        "imageCatalogPage" -> {
            val request = JsonHelper.jsonDecodeFromElement<ImageCatalogPageRequest>(params)
            MediaStoreImageCatalog.page(request.revision, request.cursor, request.limit)
        }
        "imageCatalogResolve" -> {
            val request = JsonHelper.jsonDecodeFromElement<ImageCatalogResolveRequest>(params)
            MediaStoreImageCatalog.resolve(request.revision, request.ids)
        }
        "imageCatalogVerify" -> {
            val request = JsonHelper.jsonDecodeFromElement<ImageCatalogRevisionRequest>(params)
            MediaStoreImageCatalog.verify(request.revision)
            JsonPrimitive(true)
        }
        else -> error("Unknown image index host operation: $method")
    }
}
actual suspend fun disconnectImageCatalogHost() = withContext(NonCancellable + Dispatchers.IO) { MediaStoreImageObserver.shutdown() }

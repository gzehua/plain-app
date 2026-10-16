package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.rust.request.string
import kotlinx.coroutines.*
import kotlinx.serialization.json.*

actual suspend fun handleImageCatalogHost(method: String, params: JsonObject): JsonElement = withContext(Dispatchers.IO) {
    when (method) {
        "imageCatalogSnapshot" -> MediaStoreImageCatalog.snapshot()
        "imageCatalogPage" -> MediaStoreImageCatalog.page(params.string("revision"),params.string("cursor"),params.getValue("limit").jsonPrimitive.int)
        "imageCatalogResolve" -> MediaStoreImageCatalog.resolve(params.string("revision"),params.getValue("ids").jsonArray.map { it.jsonPrimitive.content })
        "imageCatalogVerify" -> { MediaStoreImageCatalog.verify(params.string("revision"));JsonPrimitive(true) }
        else -> error("Unknown image index host operation: $method")
    }
}
actual suspend fun disconnectImageCatalogHost() = withContext(NonCancellable + Dispatchers.IO) { MediaStoreImageObserver.shutdown() }

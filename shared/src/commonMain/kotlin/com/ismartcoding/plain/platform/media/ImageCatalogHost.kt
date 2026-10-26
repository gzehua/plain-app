package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

interface ImageCatalog {
    fun snapshot(): JsonElement
    fun page(revision: String, cursor: String, limit: Int): JsonElement
    fun resolve(revision: String, ids: List<String>): JsonElement
    fun verify(revision: String)
}

internal expect val platformImageCatalog: ImageCatalog

internal suspend fun handleImageCatalogHost(method: String, params: JsonObject): JsonElement = withContext(Dispatchers.IO) {
    when (method) {
        "imageCatalogSnapshot" -> platformImageCatalog.snapshot()
        "imageCatalogPage" -> {
            val request = JsonHelper.jsonDecodeFromElement<ImageCatalogPageRequest>(params)
            platformImageCatalog.page(request.revision, request.cursor, request.limit)
        }
        "imageCatalogResolve" -> {
            val request = JsonHelper.jsonDecodeFromElement<ImageCatalogResolveRequest>(params)
            platformImageCatalog.resolve(request.revision, request.ids)
        }
        "imageCatalogVerify" -> {
            val request = JsonHelper.jsonDecodeFromElement<ImageCatalogRevisionRequest>(params)
            platformImageCatalog.verify(request.revision)
            JsonPrimitive(true)
        }
        else -> error("Unknown image index host operation: $method")
    }
}

@Serializable
internal data class ImageCatalogPageRequest(val revision: String, val cursor: String, val limit: Int)

@Serializable
internal data class ImageCatalogResolveRequest(val revision: String, val ids: List<String>)

@Serializable
internal data class ImageCatalogRevisionRequest(val revision: String)

package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.lib.JsonHelper

import com.ismartcoding.plain.platform.media.IosPhotoLibrary

import kotlinx.serialization.json.*

actual suspend fun handleImageCatalogHost(method: String, params: JsonObject): JsonElement = when (method) {
    "imageCatalogSnapshot" -> IosPhotoLibrary.snapshot()
    "imageCatalogPage" -> {
        val request = JsonHelper.jsonDecodeFromElement<ImageCatalogPageRequest>(params)
        IosPhotoLibrary.page(request.revision, request.cursor, request.limit)
    }
    "imageCatalogResolve" -> {
        val request = JsonHelper.jsonDecodeFromElement<ImageCatalogResolveRequest>(params)
        IosPhotoLibrary.resolve(request.revision, request.ids)
    }
    "imageCatalogVerify" -> {
        val request = JsonHelper.jsonDecodeFromElement<ImageCatalogRevisionRequest>(params)
        IosPhotoLibrary.verify(request.revision)
        JsonPrimitive(true)
    }
    else -> error("Unknown image index host operation: $method")
}
actual suspend fun disconnectImageCatalogHost() = IosPhotoLibrary.observe(false)

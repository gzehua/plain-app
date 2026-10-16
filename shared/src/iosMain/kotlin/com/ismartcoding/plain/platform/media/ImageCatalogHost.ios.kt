package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.platform.media.IosPhotoLibrary

import com.ismartcoding.plain.rust.request.string
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

actual suspend fun handleImageCatalogHost(method: String, params: JsonObject): JsonElement = when (method) {
    "imageCatalogSnapshot" -> IosPhotoLibrary.snapshot()
    "imageCatalogPage" -> IosPhotoLibrary.page(params.string("revision"), params.string("cursor"), params.getValue("limit").jsonPrimitive.int)
    "imageCatalogResolve" -> IosPhotoLibrary.resolve(params.string("revision"), params.getValue("ids").jsonArray.map { it.jsonPrimitive.content })
    "imageCatalogVerify" -> { IosPhotoLibrary.verify(params.string("revision")); JsonPrimitive(true) }
    else -> error("Unknown image index host operation: $method")
}
actual suspend fun disconnectImageCatalogHost() = IosPhotoLibrary.observe(false)

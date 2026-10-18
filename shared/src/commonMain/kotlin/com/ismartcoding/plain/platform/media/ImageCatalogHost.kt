package com.ismartcoding.plain.platform.media

import kotlinx.serialization.Serializable

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

expect suspend fun handleImageCatalogHost(method: String, params: JsonObject): JsonElement
expect suspend fun disconnectImageCatalogHost()

@Serializable
internal data class ImageCatalogPageRequest(val revision: String, val cursor: String, val limit: Int)

@Serializable
internal data class ImageCatalogResolveRequest(val revision: String, val ids: List<String>)

@Serializable
internal data class ImageCatalogRevisionRequest(val revision: String)

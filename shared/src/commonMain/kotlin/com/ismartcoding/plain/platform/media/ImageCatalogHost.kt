package com.ismartcoding.plain.platform.media

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

expect suspend fun handleImageCatalogHost(method: String, params: JsonObject): JsonElement
expect suspend fun disconnectImageCatalogHost()

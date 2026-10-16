package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.platform.media.handleImageCatalogHost
import com.ismartcoding.plain.platform.media.disconnectImageCatalogHost
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

object PlatformImageIndexProvider : ImageIndexProvider {
    override suspend fun handle(method: String, params: JsonObject): JsonElement = handleImageCatalogHost(method, params)
    override suspend fun disconnect() = disconnectImageCatalogHost()
}

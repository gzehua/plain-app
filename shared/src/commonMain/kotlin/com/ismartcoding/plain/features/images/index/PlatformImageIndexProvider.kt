package com.ismartcoding.plain.features.images.index

import com.ismartcoding.plain.platform.media.handleImageIndexHost
import com.ismartcoding.plain.platform.media.disconnectImageIndexHost
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

object PlatformImageIndexProvider : ImageIndexProvider {
    override suspend fun handle(method: String, params: JsonObject): JsonElement = handleImageIndexHost(method, params)
    override suspend fun disconnect() = disconnectImageIndexHost()
}

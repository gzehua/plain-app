package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.media.observeImageCatalog
import kotlinx.serialization.json.*

internal object SystemImageSearchHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemImageModelsObserve" -> {
            observeImageCatalog(JsonHelper.jsonDecodeFromElement<ObserveImageCatalogRequest>(params).enabled)
            JsonHelper.jsonEncodeToElement(true)
        }
        else -> error("Unsupported image model platform operation")
    }
}

@Serializable
private data class ObserveImageCatalogRequest(val enabled: Boolean)

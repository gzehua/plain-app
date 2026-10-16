package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.features.images.search.observeImageModels
import kotlinx.serialization.json.*

internal object SystemImageSearchHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemImageModelsObserve" -> {
            observeImageModels(params.getValue("enabled").jsonPrimitive.boolean)
            JsonHelper.jsonEncodeToElement(true)
        }
        else -> error("Unsupported image model platform operation")
    }
}

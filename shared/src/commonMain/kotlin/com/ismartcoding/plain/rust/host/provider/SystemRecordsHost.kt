package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object SystemRecordsHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemDeleteRecords" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.system.deleteSystemProviderFacts(
            com.ismartcoding.plain.platform.system.DataType.valueOf(params.getValue("provider").jsonPrimitive.content),
            params.getValue("ids").jsonArray.map { it.jsonPrimitive.content }.toSet()))
        else -> error("Unsupported provider operation")
    }
}

package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object SystemRecordsHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemDeleteRecords" -> {
            val request = JsonHelper.jsonDecodeFromElement<DeleteRecordsRequest>(params)
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.system.deleteSystemProviderFacts(
                com.ismartcoding.plain.platform.system.DataType.valueOf(request.provider), request.ids.toSet()))
        }
        else -> error("Unsupported provider operation")
    }
}

@Serializable
private data class DeleteRecordsRequest(val provider: String, val ids: List<String>)

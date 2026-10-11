package com.ismartcoding.plain.features.system

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object SystemTagsHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemTagQueryStubs" -> JsonHelper.jsonEncodeToElement(
            com.ismartcoding.plain.platform.getMediaTagRelationStubs(
                mediaDataType(params), params.getValue("query").jsonPrimitive.content).map { stub ->
                TagQueryStubFacts(
                    key = stub.key,
                    title = stub.title,
                    size = stub.size,
                )
        })
        "systemTagQueryKeys" -> JsonHelper.jsonEncodeToElement(IdsFacts(
            ids = com.ismartcoding.plain.platform.getMediaIds(
                mediaDataType(params), params.getValue("query").jsonPrimitive.content).toList(),
        ))
        else -> error("Unsupported provider operation")
    }

}

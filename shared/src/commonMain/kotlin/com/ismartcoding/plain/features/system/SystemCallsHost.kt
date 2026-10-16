package com.ismartcoding.plain.features.system

import com.ismartcoding.plain.graphql.models.Call as PhoneCall
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object SystemCallsHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemPhoneLocaleFacts" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.phoneLocaleFacts())
        "systemPhoneMetadata" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.phoneMetadata(
            JsonHelper.jsonDecodeFromElement<com.ismartcoding.plain.features.call.PhoneMetadataRequest>(params)))
        "systemCallFacts" -> {
            val plan = params.providerPlan()
            val offset = params.getValue("offset").jsonPrimitive.int
            val limit = params.getValue("limit").jsonPrimitive.int
            JsonHelper.jsonEncodeToElement(
                com.ismartcoding.plain.platform.searchMedia(
                    com.ismartcoding.plain.enums.DataType.CALL, plan, limit, offset,
                    com.ismartcoding.plain.features.file.FileSortBy.DATE_DESC,
                ).filterIsInstance<PhoneCall>().map(::callFacts)
            )
        }
        "systemCallCount" -> JsonHelper.jsonEncodeToElement(
            com.ismartcoding.plain.platform.countMedia(
                com.ismartcoding.plain.enums.DataType.CALL,
                params.providerPlan(),
            )
        )
        "systemCallIds" -> JsonHelper.jsonEncodeToElement(
            com.ismartcoding.plain.platform.getMediaIds(
                com.ismartcoding.plain.enums.DataType.CALL,
                params.providerPlan(),
            )
        )
        "systemMakeCall" -> {
            com.ismartcoding.plain.platform.call(
                params.getValue("number").jsonPrimitive.content,
                params.getValue("showDialer").jsonPrimitive.boolean,
            )
            JsonHelper.jsonEncodeToElement(true)
        }
        else -> error("Unsupported provider operation")
    }

    private fun callFacts(call: PhoneCall) = CallFacts(
        id = call.id, number = call.number, name = call.name, photoUri = call.photoUri,
        startedAt = call.startedAt.toString(), durationSec = call.durationSec, type = call.typeCode, accountId = call.accountId,
    )
}

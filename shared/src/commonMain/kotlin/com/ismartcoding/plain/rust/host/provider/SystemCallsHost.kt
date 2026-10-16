package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.api.graphql.models.Call as PhoneCall
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object SystemCallsHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemPhoneLocaleFacts" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.contacts.phoneLocaleFacts())
        "systemPhoneMetadata" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.contacts.phoneMetadata(
            JsonHelper.jsonDecodeFromElement<com.ismartcoding.plain.features.telephony.calls.PhoneMetadataRequest>(params)))
        "systemCallFacts" -> {
            val plan = params.providerPlan()
            val offset = params.getValue("offset").jsonPrimitive.int
            val limit = params.getValue("limit").jsonPrimitive.int
            JsonHelper.jsonEncodeToElement(
                com.ismartcoding.plain.platform.media.searchMedia(
                    com.ismartcoding.plain.platform.system.DataType.CALL, plan, limit, offset,
                    com.ismartcoding.plain.features.files.FileSortBy.DATE_DESC,
                ).filterIsInstance<PhoneCall>().map(::callFacts)
            )
        }
        "systemCallCount" -> JsonHelper.jsonEncodeToElement(
            com.ismartcoding.plain.platform.media.countMedia(
                com.ismartcoding.plain.platform.system.DataType.CALL,
                params.providerPlan(),
            )
        )
        "systemCallIds" -> JsonHelper.jsonEncodeToElement(
            com.ismartcoding.plain.platform.media.getMediaIds(
                com.ismartcoding.plain.platform.system.DataType.CALL,
                params.providerPlan(),
            )
        )
        "systemMakeCall" -> {
            com.ismartcoding.plain.platform.media.call(
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

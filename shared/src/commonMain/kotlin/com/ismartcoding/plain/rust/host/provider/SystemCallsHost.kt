package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

import com.ismartcoding.plain.models.Call as PhoneCall
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object SystemCallsHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemPhoneLocaleFacts" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.contacts.phoneLocaleFacts())
        "systemCallFacts" -> {
            val request = JsonHelper.jsonDecodeFromElement<ProviderPageRequest>(params)
            JsonHelper.jsonEncodeToElement(
                com.ismartcoding.plain.platform.media.searchMedia(
                    com.ismartcoding.plain.platform.system.DataType.CALL, request.plan, request.limit, request.offset,
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
            val request = JsonHelper.jsonDecodeFromElement<MakeCallRequest>(params)
            com.ismartcoding.plain.platform.media.call(
                request.number, request.showDialer,
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

@Serializable
private data class MakeCallRequest(val number: String, val showDialer: Boolean)

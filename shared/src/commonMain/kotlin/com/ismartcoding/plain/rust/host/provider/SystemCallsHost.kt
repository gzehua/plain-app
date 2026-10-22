package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.models.Call as PhoneCall
import com.ismartcoding.plain.platform.contacts.phoneLocaleFacts
import com.ismartcoding.plain.platform.media.call
import com.ismartcoding.plain.platform.media.countMedia
import com.ismartcoding.plain.platform.media.getMediaIds
import com.ismartcoding.plain.platform.media.searchMedia
import com.ismartcoding.plain.platform.system.DataType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

internal object SystemCallsHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemPhoneLocaleFacts" -> JsonHelper.jsonEncodeToElement(phoneLocaleFacts())
        "systemCallFacts" -> {
            val request = JsonHelper.jsonDecodeFromElement<ProviderPageRequest>(params)
            JsonHelper.jsonEncodeToElement(
                searchMedia(
                    DataType.CALL, request.plan, request.limit, request.offset,
                    FileSortBy.DATE_DESC,
                ).filterIsInstance<PhoneCall>().map(::callFacts)
            )
        }
        "systemCallCount" -> JsonHelper.jsonEncodeToElement(
            countMedia(
                DataType.CALL,
                params.providerPlan(),
            )
        )
        "systemCallIds" -> JsonHelper.jsonEncodeToElement(
            getMediaIds(
                DataType.CALL,
                params.providerPlan(),
            )
        )
        "systemMakeCall" -> {
            val request = JsonHelper.jsonDecodeFromElement<MakeCallRequest>(params)
            call(
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

package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

import com.ismartcoding.plain.features.telephony.sms.MmsLaunchRequest
import com.ismartcoding.plain.features.telephony.sms.MmsProviderRequest
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object SystemSmsHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemMmsTextFacts", "systemSmsCountFacts", "systemSmsRowsFacts", "systemSmsIdsFacts", "systemSmsConversationFacts", "systemSmsThreadFacts" -> com.ismartcoding.plain.platform.system.systemSmsFacts(method, params)
        "systemDivideSmsText" -> {
            val request = JsonHelper.jsonDecodeFromElement<DivideSmsTextRequest>(params)
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.media.divideSmsText(request.body, request.subscriptionId))
        }
        "systemSendSmsParts" -> {
            com.ismartcoding.plain.platform.media.sendSmsParts(JsonHelper.jsonDecodeFromElement(params))
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemDeleteSmsRows" -> {
            val request = JsonHelper.jsonDecodeFromElement<com.ismartcoding.plain.features.telephony.sms.SmsDeleteRowsRequest>(params)
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.media.deleteSmsRows(request.ids))
        }
        "systemMmsLatest" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.media.getLatestSentMmsId())
        "systemMmsLaunch" -> {
            val request = JsonHelper.jsonDecodeFromElement<MmsLaunchRequest>(params)
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.media.launchDefaultSmsApp(request.number, request.body,
                request.attachments.map { it.path to it.contentType }))
        }
        "systemMmsCandidates" -> {
            val request = JsonHelper.jsonDecodeFromElement<MmsProviderRequest>(params)
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.media.readSentMmsCandidates(request.minimumId, request.launchTimeSec))
        }
        "systemSimFacts" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.telephony.getSims().map { sim ->
            SimFacts(
                id = sim.id,
                label = sim.label,
                number = sim.number,
                subscriptionId = sim.subscriptionId,
            )
        })
        else -> error("Unsupported provider operation")
    }
}

@Serializable
private data class DivideSmsTextRequest(val body: String, val subscriptionId: Int? = null)

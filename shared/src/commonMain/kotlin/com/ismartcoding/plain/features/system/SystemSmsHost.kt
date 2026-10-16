package com.ismartcoding.plain.features.system

import com.ismartcoding.plain.features.sms.MmsLaunchRequest
import com.ismartcoding.plain.features.sms.MmsProviderRequest
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object SystemSmsHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemMmsTextFacts", "systemSmsCountFacts", "systemSmsRowsFacts", "systemSmsIdsFacts", "systemSmsConversationFacts", "systemSmsThreadFacts" -> com.ismartcoding.plain.platform.systemSmsFacts(method, params)
        "systemDivideSmsText" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.divideSmsText(
            params.getValue("body").jsonPrimitive.content, params["subscriptionId"]?.takeUnless { it is JsonNull }?.jsonPrimitive?.int))
        "systemSendSmsParts" -> {
            com.ismartcoding.plain.platform.sendSmsParts(JsonHelper.jsonDecodeFromElement(params))
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemDeleteSmsRows" -> {
            val request = JsonHelper.jsonDecodeFromElement<com.ismartcoding.plain.features.sms.SmsDeleteRowsRequest>(params)
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.deleteSmsRows(request.ids))
        }
        "systemMmsLatest" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.getLatestSentMmsId())
        "systemMmsLaunch" -> {
            val request = JsonHelper.jsonDecodeFromElement<MmsLaunchRequest>(params)
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.launchDefaultSmsApp(request.number, request.body,
                request.attachments.map { it.path to it.contentType }))
        }
        "systemMmsCandidates" -> {
            val request = JsonHelper.jsonDecodeFromElement<MmsProviderRequest>(params)
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.readSentMmsCandidates(request.minimumId, request.launchTimeSec))
        }
        "systemSimFacts" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.getSims().map { sim ->
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

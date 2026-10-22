package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.features.telephony.sms.MmsLaunchRequest
import com.ismartcoding.plain.features.telephony.sms.MmsProviderRequest
import com.ismartcoding.plain.features.telephony.sms.SmsDeleteRowsRequest
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.media.deleteSmsRows
import com.ismartcoding.plain.platform.media.divideSmsText
import com.ismartcoding.plain.platform.media.getLatestSentMmsId
import com.ismartcoding.plain.platform.media.launchDefaultSmsApp
import com.ismartcoding.plain.platform.media.readSentMmsCandidates
import com.ismartcoding.plain.platform.media.sendSmsParts
import com.ismartcoding.plain.platform.system.systemSmsFacts
import com.ismartcoding.plain.platform.telephony.getSims
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

internal object SystemSmsHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemMmsTextFacts", "systemSmsCountFacts", "systemSmsRowsFacts", "systemSmsIdsFacts", "systemSmsConversationFacts", "systemSmsThreadFacts" -> systemSmsFacts(method, params)
        "systemDivideSmsText" -> {
            val request = JsonHelper.jsonDecodeFromElement<DivideSmsTextRequest>(params)
            JsonHelper.jsonEncodeToElement(divideSmsText(request.body, request.subscriptionId))
        }
        "systemSendSmsParts" -> {
            sendSmsParts(JsonHelper.jsonDecodeFromElement(params))
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemDeleteSmsRows" -> {
            val request = JsonHelper.jsonDecodeFromElement<SmsDeleteRowsRequest>(params)
            JsonHelper.jsonEncodeToElement(deleteSmsRows(request.ids))
        }
        "systemMmsLatest" -> JsonHelper.jsonEncodeToElement(getLatestSentMmsId())
        "systemMmsLaunch" -> {
            val request = JsonHelper.jsonDecodeFromElement<MmsLaunchRequest>(params)
            JsonHelper.jsonEncodeToElement(launchDefaultSmsApp(request.number, request.body,
                request.attachments.map { it.path to it.contentType }))
        }
        "systemMmsCandidates" -> {
            val request = JsonHelper.jsonDecodeFromElement<MmsProviderRequest>(params)
            JsonHelper.jsonEncodeToElement(readSentMmsCandidates(request.minimumId, request.launchTimeSec))
        }
        "systemSimFacts" -> JsonHelper.jsonEncodeToElement(getSims())
        else -> error("Unsupported provider operation")
    }
}

@Serializable
private data class DivideSmsTextRequest(val body: String, val subscriptionId: Int? = null)

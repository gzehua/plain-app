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
import com.ismartcoding.plain.rust.host.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

internal object SystemSmsHost {
    internal suspend fun handle(method: String, params: JsonObject): HostResult = when (method) {
        "systemMmsTextFacts", "systemSmsCountFacts", "systemSmsRowsFacts", "systemSmsIdsFacts", "systemSmsConversationFacts", "systemSmsThreadFacts" -> hostResult(systemSmsFacts(method, params))
        "systemDivideSmsText" -> {
            val request = JsonHelper.jsonDecodeFromElement<DivideSmsTextRequest>(params)
            hostResult(divideSmsText(request.body, request.subscriptionId))
        }
        "systemSendSmsParts" -> {
            sendSmsParts(JsonHelper.jsonDecodeFromElement(params))
            hostResult(true)
        }
        "systemDeleteSmsRows" -> {
            val request = JsonHelper.jsonDecodeFromElement<SmsDeleteRowsRequest>(params)
            hostResult(deleteSmsRows(request.ids))
        }
        "systemMmsLatest" -> hostResult(getLatestSentMmsId())
        "systemMmsLaunch" -> {
            val request = JsonHelper.jsonDecodeFromElement<MmsLaunchRequest>(params)
            hostResult(launchDefaultSmsApp(request.number, request.body,
                request.attachments.map { it.path to it.contentType }))
        }
        "systemMmsCandidates" -> {
            val request = JsonHelper.jsonDecodeFromElement<MmsProviderRequest>(params)
            hostResult(readSentMmsCandidates(request.minimumId, request.launchTimeSec))
        }
        else -> error("Unsupported provider operation")
    }
}

@Serializable
private data class DivideSmsTextRequest(val body: String, val subscriptionId: Int? = null)

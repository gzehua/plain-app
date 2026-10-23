package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.models.File as FileEntry

import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.features.telephony.sms.SmsConversationFacts
import com.ismartcoding.plain.features.telephony.sms.SmsCountFacts
import com.ismartcoding.plain.features.telephony.sms.SmsRowsFacts
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.rust.host.provider.MediaBucketItemFacts
import com.ismartcoding.plain.rust.host.provider.ProviderPlan
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

actual suspend fun deleteSystemProviderFacts(provider: DataType, ids: Set<String>): Set<String> = emptySet()

actual suspend fun systemSmsFacts(method: String, params: JsonObject): JsonElement =
    when (method) {
        "systemMmsTextFacts" -> JsonHelper.jsonEncodeToElement(emptyMap<String, String>())
        "systemSmsIdsFacts" -> JsonHelper.jsonEncodeToElement(emptyList<String>())
        "systemSmsCountFacts" -> JsonHelper.jsonEncodeToElement(
            SmsCountFacts(0, 0))
        "systemSmsRowsFacts" -> JsonHelper.jsonEncodeToElement(
            SmsRowsFacts(emptyList(), emptyList(), emptyList()))
        "systemSmsConversationFacts" -> JsonHelper.jsonEncodeToElement(
            SmsConversationFacts(emptyList(), emptyList(), emptyMap()))
        "systemSmsThreadFacts" -> JsonHelper.jsonEncodeToElement(emptyList<Pair<String, String>>())
        else -> error("Unsupported SMS facts")
    }

actual suspend fun mediaBucketItemFacts(dataType: DataType, plan: ProviderPlan): List<MediaBucketItemFacts> = emptyList()

actual suspend fun fileIndexId(path: String): String? = null
actual suspend fun fileIndexRows(plan: ProviderPlan, limit: Int, offset: Int, sortBy: FileSortBy): List<FileEntry> = emptyList()
actual suspend fun fileIndexCount(plan: ProviderPlan): Int = 0

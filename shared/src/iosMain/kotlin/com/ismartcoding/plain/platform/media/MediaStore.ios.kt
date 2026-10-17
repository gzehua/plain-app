package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.platform.system.appDir

import com.ismartcoding.plain.db.IData
import com.ismartcoding.plain.rust.host.provider.ProviderPlan
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.features.files.FileSortBy
import kotlinx.cinterop.useContents

actual fun getMediaItemUriString(dataType: DataType, id: String): String = ""

actual suspend fun getMediaPathsByIds(dataType: DataType, ids: Set<String>): Set<String> = emptySet()

actual suspend fun deleteSmsRows(ids: List<String>): com.ismartcoding.plain.features.telephony.sms.SmsDeletionReceipt =
    throw UnsupportedOperationException("SMS provider is unavailable")

actual fun divideSmsText(body: String, subscriptionId: Int?): List<String> = error("SMS provider is unavailable")
actual fun sendSmsParts(request: com.ismartcoding.plain.features.telephony.sms.SmsDispatchRequest): Unit = error("SMS provider is unavailable")

actual fun call(number: String, showDialer: Boolean) {}

actual fun mimeTypeFromExtension(extension: String): String = "application/octet-stream"

actual fun launchDefaultSmsApp(
    number: String,
    body: String,
    attachments: List<Pair<String, String>>,
): Long = 0L

actual fun getLatestSentMmsId(): Long = 0L

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun getScreenSize(): Pair<Int, Int> {
    val bounds = platform.UIKit.UIScreen.mainScreen.bounds.useContents {
        Pair(size.width.toInt(), size.height.toInt())
    }
    return bounds
}

internal actual fun platformDownloadsDirPath(): String = appDir() + "/downloads"

actual fun updateContact(id: String, input: com.ismartcoding.plain.features.contacts.ContactInput) {}

actual fun createContact(input: com.ismartcoding.plain.features.contacts.ContactInput, accountType: String): String = ""

actual suspend fun deleteContacts(ids: Set<String>) {}

actual fun readSentMmsCandidates(minimumId: Long, launchTimeSec: Long): List<com.ismartcoding.plain.features.telephony.sms.MmsCandidateFacts> = emptyList()

actual suspend fun searchMedia(dataType: DataType, plan: ProviderPlan, limit: Int, offset: Int, sortBy: FileSortBy): List<IData> = emptyList()
actual suspend fun countMedia(dataType: DataType, plan: ProviderPlan): Int = 0
actual suspend fun getMediaIds(dataType: DataType, plan: ProviderPlan): Set<String> = emptySet()

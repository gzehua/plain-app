package com.ismartcoding.plain.platform

import com.ismartcoding.plain.ai.ImageSearchStatusType
import com.ismartcoding.plain.data.TagRelationStub



import com.ismartcoding.plain.db.IData
import com.ismartcoding.plain.features.system.ProviderPlan
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.features.file.FileSortBy
import kotlinx.cinterop.useContents


actual suspend fun trashMedia(dataType: DataType, ids: Set<String>) {
    com.ismartcoding.plain.features.mediaactions.MediaActionHelper.run(dataType,com.ismartcoding.plain.features.mediaactions.MediaAction.TRASH,ids)
}

actual suspend fun restoreMedia(dataType: DataType, ids: Set<String>) {
    com.ismartcoding.plain.features.mediaactions.MediaActionHelper.run(dataType,com.ismartcoding.plain.features.mediaactions.MediaAction.RESTORE,ids)
}

actual suspend fun deleteMedia(dataType: DataType, ids: Set<String>, fromTrash: Boolean) {
    com.ismartcoding.plain.features.mediaactions.MediaActionHelper.run(dataType,com.ismartcoding.plain.features.mediaactions.MediaAction.DELETE,ids,fromTrash)
}

actual suspend fun moveMedia(dataType: DataType, ids: Set<String>, destDir: String): Boolean =
    com.ismartcoding.plain.features.mediaactions.MediaActionHelper.run(dataType,com.ismartcoding.plain.features.mediaactions.MediaAction.MOVE,ids,destDir=destDir) == ids.size





actual fun getMediaItemUriString(dataType: DataType, id: String): String = ""



actual suspend fun getMediaPathsByIds(dataType: DataType, ids: Set<String>): Set<String> = emptySet()





actual suspend fun deleteSmsRows(ids: List<String>): com.ismartcoding.plain.features.sms.SmsDeletionReceipt =
    throw UnsupportedOperationException("SMS provider is unavailable")

actual fun divideSmsText(body: String, subscriptionId: Int?): List<String> = error("SMS provider is unavailable")
actual fun sendSmsParts(request: com.ismartcoding.plain.features.sms.SmsDispatchRequest): Unit = error("SMS provider is unavailable")


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


actual fun updateContact(id: String, input: com.ismartcoding.plain.features.contact.ContactInput) {}

actual fun createContact(input: com.ismartcoding.plain.features.contact.ContactInput): String = ""

actual suspend fun deleteContacts(ids: Set<String>) {}

actual fun readSentMmsCandidates(minimumId: Long, launchTimeSec: Long): List<com.ismartcoding.plain.features.sms.MmsCandidateFacts> = emptyList()




actual suspend fun searchMedia(dataType: DataType, plan: ProviderPlan, limit: Int, offset: Int, sortBy: FileSortBy): List<IData> = emptyList()
actual suspend fun countMedia(dataType: DataType, plan: ProviderPlan): Int = 0
actual suspend fun getMediaIds(dataType: DataType, plan: ProviderPlan): Set<String> = emptySet()

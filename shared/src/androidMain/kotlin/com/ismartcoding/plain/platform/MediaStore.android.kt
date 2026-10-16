package com.ismartcoding.plain.platform

import com.ismartcoding.plain.data.TagRelationStub



import android.provider.Telephony
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.audio.AudioMediaStoreHelper
import com.ismartcoding.plain.db.IData
import com.ismartcoding.plain.docs.DocMediaStoreHelper
import com.ismartcoding.plain.features.system.ProviderPlan
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.features.media.CallMediaStoreHelper
import com.ismartcoding.plain.features.media.ContactMediaStoreHelper
import com.ismartcoding.plain.features.media.ImageMediaStoreHelper
import com.ismartcoding.plain.features.media.VideoMediaStoreHelper
import com.ismartcoding.plain.features.sms.SmsHelper
import com.ismartcoding.plain.features.sms.SmsProviderContract
import com.ismartcoding.plain.features.file.FileSortBy
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.CancellationException


actual suspend fun trashMedia(dataType: DataType, ids: Set<String>) {
    com.ismartcoding.plain.features.mediaactions.MediaActionHelper.run(dataType,com.ismartcoding.plain.features.mediaactions.MediaAction.TRASH,ids)
}

actual suspend fun restoreMedia(dataType: DataType, ids: Set<String>) {
    com.ismartcoding.plain.features.mediaactions.MediaActionHelper.run(dataType,com.ismartcoding.plain.features.mediaactions.MediaAction.RESTORE,ids)
}

actual suspend fun deleteMedia(dataType: DataType, ids: Set<String>, fromTrash: Boolean) {
    com.ismartcoding.plain.features.mediaactions.MediaActionHelper.run(dataType,com.ismartcoding.plain.features.mediaactions.MediaAction.DELETE,ids,fromTrash)
}

actual suspend fun moveMedia(dataType: DataType, ids: Set<String>, destDir: String): Boolean {
    return com.ismartcoding.plain.features.mediaactions.MediaActionHelper.run(dataType,com.ismartcoding.plain.features.mediaactions.MediaAction.MOVE,ids,destDir=destDir) == ids.size
}

actual suspend fun deleteSmsRows(ids: List<String>): com.ismartcoding.plain.features.sms.SmsDeletionReceipt = com.ismartcoding.plain.lib.withIO {
    check(ShizukuHelper.isGranted()) { "Shizuku is not available or not granted" }
    val deleted = mutableListOf<String>()
    val failed = mutableListOf<String>()
    for (id in ids) {
        kotlinx.coroutines.currentCoroutineContext().ensureActive()
        val raw = id.removePrefix("mms_")
        val number = raw.toLongOrNull()
        if (number == null || number < 0) {
            failed.add(id)
            continue
        }
        val provider = if (id.startsWith("mms_")) Telephony.Mms.CONTENT_URI else Telephony.Sms.CONTENT_URI
        val uri = android.content.ContentUris.withAppendedId(provider, number)
        val removed = try {
            ShizukuHelper.exec("content delete --uri $uri")
            appContext.contentResolver.query(uri, arrayOf("_id"), null, null, null)?.use { !it.moveToFirst() } == true
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            com.ismartcoding.plain.lib.logcat.LogCat.e("SMS provider deletion failed", error)
            false
        }
        if (removed) deleted.add(id) else failed.add(id)
    }
    com.ismartcoding.plain.features.sms.SmsDeletionReceipt(deleted, failed)
}





actual fun getMediaItemUriString(dataType: DataType, id: String): String = when (dataType) {
    DataType.IMAGE -> ImageMediaStoreHelper.getItemUri(id).toString()
    DataType.VIDEO -> VideoMediaStoreHelper.getItemUri(id).toString()
    DataType.AUDIO -> AudioMediaStoreHelper.getItemUri(id).toString()
    DataType.DOC -> DocMediaStoreHelper.getItemUri(id).toString()
    else -> ""
}



actual suspend fun getMediaPathsByIds(dataType: DataType, ids: Set<String>): Set<String> = when (dataType) {
    DataType.AUDIO -> AudioMediaStoreHelper.getPathsByIdsAsync(appContext, ids)
    DataType.VIDEO -> VideoMediaStoreHelper.getPathsByIdsAsync(appContext, ids)
    DataType.IMAGE -> ImageMediaStoreHelper.getPathsByIdsAsync(appContext, ids)
    DataType.DOC -> DocMediaStoreHelper.getPathsByIdsAsync(appContext, ids)
    else -> emptySet()
}





actual fun divideSmsText(body: String, subscriptionId: Int?): List<String> = SmsHelper.divideText(body, subscriptionId)
actual fun sendSmsParts(request: com.ismartcoding.plain.features.sms.SmsDispatchRequest) = SmsHelper.sendParts(request)


actual fun call(number: String, showDialer: Boolean) =
    com.ismartcoding.plain.features.media.CallMediaStoreHelper.call(appContext, number, showDialer)


actual fun mimeTypeFromExtension(extension: String): String =
    android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"

actual fun launchDefaultSmsApp(
    number: String,
    body: String,
    attachments: List<Pair<String, String>>,
): Long {
    com.ismartcoding.plain.features.sms.MmsHelper.launchDefaultSmsApp(number, body, attachments)
    return System.currentTimeMillis() / 1000 - 1
}

actual fun getLatestSentMmsId(): Long {
    return runCatching {
        appContext.contentResolver.query(
            Telephony.Mms.CONTENT_URI,
            arrayOf(Telephony.Mms._ID),
            "${Telephony.Mms.MESSAGE_BOX} = 2 AND m_type = ${SmsProviderContract.MMS_PDU_SEND_REQ}",
            null,
            "${Telephony.Mms._ID} DESC LIMIT 1",
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getLong(0) else 0L
        } ?: 0L
    }.getOrDefault(0L)
}

actual fun getScreenSize(): Pair<Int, Int> {
    val displayMetrics = appContext.resources.displayMetrics
    return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
}

internal actual fun platformDownloadsDirPath(): String =
    android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS).absolutePath


actual fun updateContact(id: String, input: com.ismartcoding.plain.features.contact.ContactInput) =
    com.ismartcoding.plain.features.media.ContactMediaStoreHelper.updateAsync(id, input)

actual fun createContact(input: com.ismartcoding.plain.features.contact.ContactInput): String =
    com.ismartcoding.plain.features.media.ContactMediaStoreHelper.createAsync(input)

actual suspend fun deleteContacts(ids: Set<String>) {
    com.ismartcoding.plain.features.media.ContactMediaStoreHelper.deleteByIdsAsync(appContext, ids)
}

actual fun readSentMmsCandidates(minimumId: Long, launchTimeSec: Long): List<com.ismartcoding.plain.features.sms.MmsCandidateFacts> {
    val context = appContext
    return context.contentResolver.query(
        Telephony.Mms.CONTENT_URI,
        arrayOf(Telephony.Mms._ID, Telephony.Mms.THREAD_ID),
        "${Telephony.Mms.MESSAGE_BOX} = 2 AND m_type = ${SmsProviderContract.MMS_PDU_SEND_REQ} AND ${Telephony.Mms._ID} > ? AND ${Telephony.Mms.DATE} >= ?",
        arrayOf(minimumId.toString(), launchTimeSec.toString()),
        "${Telephony.Mms._ID} ASC",
    )?.use { cursor ->
        val idIndex = cursor.getColumnIndexOrThrow(Telephony.Mms._ID)
        val threadIndex = cursor.getColumnIndexOrThrow(Telephony.Mms.THREAD_ID)
        buildList {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val (body, attachments) = SmsHelper.readMmsBodyAndAttachments(context, id.toString())
                add(com.ismartcoding.plain.features.sms.MmsCandidateFacts(id,
                    SmsHelper.readMmsAddresses(context, id.toString()), body,
                    cursor.getString(threadIndex).orEmpty(), attachments.map { it.contentType }))
            }
        }
    } ?: emptyList()
}




actual suspend fun searchMedia(dataType: DataType, plan: ProviderPlan, limit: Int, offset: Int, sortBy: FileSortBy): List<IData> {
    if (dataType == DataType.CALL && !Permission.READ_CALL_LOG.isGranted()) return emptyList()
    if (dataType == DataType.CONTACT && !Permission.READ_CONTACTS.isGranted()) return emptyList()
    val where = plan.where()
    return when (dataType) {
        DataType.AUDIO -> AudioMediaStoreHelper.searchAsync(appContext, where, limit, offset, sortBy)
        DataType.DOC -> DocMediaStoreHelper.searchAsync(appContext, where, limit, offset, sortBy)
        DataType.IMAGE -> ImageMediaStoreHelper.searchAsync(appContext, where, limit, offset, sortBy)
        DataType.VIDEO -> VideoMediaStoreHelper.searchAsync(appContext, where, limit, offset, sortBy)
        DataType.CALL -> CallMediaStoreHelper.searchAsync(appContext, where, limit, offset)
        DataType.CONTACT -> ContactMediaStoreHelper.searchAsync(appContext, where, limit, offset)
        else -> error("Unsupported provider: $dataType")
    }
}

actual suspend fun countMedia(dataType: DataType, plan: ProviderPlan): Int {
    if (dataType == DataType.CALL && !Permission.READ_CALL_LOG.isGranted()) return 0
    if (dataType == DataType.CONTACT && !Permission.READ_CONTACTS.isGranted()) return 0
    return plan.wheres().sumOf { where ->
        when (dataType) {
            DataType.AUDIO -> AudioMediaStoreHelper.countAsync(appContext, where)
            DataType.DOC -> DocMediaStoreHelper.countAsync(appContext, where)
            DataType.IMAGE -> ImageMediaStoreHelper.countAsync(appContext, where)
            DataType.VIDEO -> VideoMediaStoreHelper.countAsync(appContext, where)
            DataType.CALL -> CallMediaStoreHelper.countAsync(appContext, where)
            DataType.CONTACT -> ContactMediaStoreHelper.countAsync(appContext, where)
            else -> error("Unsupported provider: $dataType")
        }
    }
}

actual suspend fun getMediaIds(dataType: DataType, plan: ProviderPlan): Set<String> {
    if (dataType == DataType.CALL && !Permission.READ_CALL_LOG.isGranted()) return emptySet()
    if (dataType == DataType.CONTACT && !Permission.READ_CONTACTS.isGranted()) return emptySet()
    return when (dataType) {
        DataType.AUDIO -> AudioMediaStoreHelper.getIdsAsync(appContext, plan.where())
        DataType.DOC -> DocMediaStoreHelper.getIdsAsync(appContext, plan.where())
        DataType.IMAGE -> ImageMediaStoreHelper.getIdsAsync(appContext, plan.where())
        DataType.VIDEO -> VideoMediaStoreHelper.getIdsAsync(appContext, plan.where())
        DataType.CALL -> CallMediaStoreHelper.getIdsAsync(appContext, plan.where())
        DataType.CONTACT -> ContactMediaStoreHelper.getIdsAsync(appContext, plan.where())
        else -> error("Unsupported provider: $dataType")
    }
}

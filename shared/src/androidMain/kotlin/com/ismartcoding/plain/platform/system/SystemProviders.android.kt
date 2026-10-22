package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.platform.isQPlus

import com.ismartcoding.plain.models.File as FileEntry

import android.provider.CallLog
import android.provider.ContactsContract
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.lib.withIO

actual suspend fun deleteSystemProviderFacts(provider: DataType, ids: Set<String>): Set<String> = withIO {
    val uri = when (provider) {
        DataType.CALL -> CallLog.Calls.CONTENT_URI
        DataType.CONTACT -> ContactsContract.RawContacts.CONTENT_URI
        else -> error("Unsupported system provider")
    }
    ids.filter { id ->
        val number = id.toLongOrNull() ?: return@filter false
        val arguments = arrayOf(number.toString())
        val selection = if (provider == DataType.CONTACT) "_id = ? AND deleted = 0" else "_id = ?"
        appContext.contentResolver.delete(uri, "_id = ?", arguments) > 0 &&
            appContext.contentResolver.query(uri, arrayOf("_id"), selection, arguments, null)?.use { !it.moveToFirst() } == true
    }.toSet()
}

actual suspend fun systemSmsFacts(method: String, params: kotlinx.serialization.json.JsonObject): kotlinx.serialization.json.JsonElement =
    com.ismartcoding.plain.features.telephony.sms.SmsHelper.facts(appContext, method, params)

actual suspend fun mediaBucketItemFacts(dataType: DataType, plan: com.ismartcoding.plain.rust.host.provider.ProviderPlan): List<com.ismartcoding.plain.rust.host.provider.MediaBucketItemFacts> = when (dataType) {
    DataType.IMAGE -> com.ismartcoding.plain.platform.media.provider.ImageMediaStoreHelper.getBucketItemFactsAsync(appContext, plan.where())
    DataType.VIDEO -> com.ismartcoding.plain.platform.media.provider.VideoMediaStoreHelper.getBucketItemFactsAsync(appContext, plan.where())
    DataType.AUDIO -> if (com.ismartcoding.plain.platform.isQPlus()) com.ismartcoding.plain.features.audio.AudioMediaStoreHelper.getBucketItemFactsAsync(appContext, plan.where()) else emptyList()
    DataType.DOC -> if (isQPlus()) com.ismartcoding.plain.platform.media.provider.DocMediaStoreHelper.getBucketItemFactsAsync(appContext, plan.where()) else emptyList()
    else -> emptyList()
}

actual suspend fun fileIndexId(path: String): String? = com.ismartcoding.plain.lib.withIO { com.ismartcoding.plain.platform.media.provider.FileMediaStoreHelper.getIdByPathAsync(com.ismartcoding.plain.appContext, path) }
actual suspend fun fileIndexRows(plan: com.ismartcoding.plain.rust.host.provider.ProviderPlan, limit: Int, offset: Int, sortBy: com.ismartcoding.plain.features.files.FileSortBy): List<com.ismartcoding.plain.models.File> = com.ismartcoding.plain.platform.media.provider.FileMediaStoreHelper.searchAsync(com.ismartcoding.plain.appContext, plan.where(), limit, offset, sortBy)
actual suspend fun fileIndexCount(plan: com.ismartcoding.plain.rust.host.provider.ProviderPlan): Int = com.ismartcoding.plain.lib.withIO {
    plan.wheres().sumOf { com.ismartcoding.plain.platform.media.provider.FileMediaStoreHelper.countAsync(com.ismartcoding.plain.appContext, it) }
}

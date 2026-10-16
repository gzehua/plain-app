package com.ismartcoding.plain.platform

import com.ismartcoding.plain.graphql.models.File as FileEntry

import android.content.ContentUris
import android.provider.CallLog
import android.provider.ContactsContract
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.lib.withIO

actual suspend fun deleteSystemProviderFacts(provider: DataType, ids: Set<String>): Set<String> = withIO {
    val uri = when (provider) {
        DataType.CALL -> CallLog.Calls.CONTENT_URI
        DataType.CONTACT -> ContactsContract.RawContacts.CONTENT_URI
        else -> error("Unsupported system provider")
    }
    ids.filter { id ->
        val number = id.toLongOrNull() ?: return@filter false
        val row = ContentUris.withAppendedId(uri, number)
        runCatching {
            appContext.contentResolver.delete(row, null, null) > 0 &&
                appContext.contentResolver.query(row, arrayOf("_id"), null, null, null)?.use { !it.moveToFirst() } == true
        }.getOrDefault(false)
    }.toSet()
}

actual suspend fun systemSmsFacts(method: String, params: kotlinx.serialization.json.JsonObject): kotlinx.serialization.json.JsonElement =
    com.ismartcoding.plain.features.sms.SmsHelper.facts(appContext, method, params)

actual suspend fun mediaBucketItemFacts(dataType: DataType): List<com.ismartcoding.plain.features.system.MediaBucketItemFacts> = when (dataType) {
    DataType.IMAGE -> com.ismartcoding.plain.features.media.ImageMediaStoreHelper.getBucketItemFactsAsync(appContext)
    DataType.VIDEO -> com.ismartcoding.plain.features.media.VideoMediaStoreHelper.getBucketItemFactsAsync(appContext)
    DataType.AUDIO -> if (com.ismartcoding.plain.platform.isQPlus()) com.ismartcoding.plain.audio.AudioMediaStoreHelper.getBucketItemFactsAsync(appContext) else emptyList()
    DataType.DOC -> com.ismartcoding.plain.docs.DocMediaStoreHelper.getDocBucketItemFactsAsync(appContext)
    else -> emptyList()
}

actual suspend fun fileIndexId(path: String): String? = com.ismartcoding.plain.lib.withIO { com.ismartcoding.plain.features.media.FileMediaStoreHelper.getIdByPathAsync(com.ismartcoding.plain.appContext, path) }
actual suspend fun fileIndexRows(plan: com.ismartcoding.plain.features.system.ProviderPlan, limit: Int, offset: Int, sortBy: com.ismartcoding.plain.features.file.FileSortBy): List<com.ismartcoding.plain.graphql.models.File> = com.ismartcoding.plain.features.media.FileMediaStoreHelper.searchAsync(com.ismartcoding.plain.appContext, plan.where(), limit, offset, sortBy)
actual suspend fun fileIndexCount(plan: com.ismartcoding.plain.features.system.ProviderPlan): Int = com.ismartcoding.plain.lib.withIO {
    plan.wheres().sumOf { com.ismartcoding.plain.features.media.FileMediaStoreHelper.countAsync(com.ismartcoding.plain.appContext, it) }
}

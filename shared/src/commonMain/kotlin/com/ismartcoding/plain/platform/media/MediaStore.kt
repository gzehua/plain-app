package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.IData
import com.ismartcoding.plain.rust.host.provider.ProviderPlan
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.features.files.FileSortBy

/**
 * Move media items of [dataType] identified by [ids] to the trash.
 */
suspend fun trashMedia(dataType: DataType, ids: Set<String>) {
    com.ismartcoding.plain.features.media.actions.MediaActionClient.run(dataType, com.ismartcoding.plain.features.media.actions.MediaAction.TRASH, ids)
}

/**
 * Restore media items of [dataType] identified by [ids] from the trash.
 */
suspend fun restoreMedia(dataType: DataType, ids: Set<String>) {
    com.ismartcoding.plain.features.media.actions.MediaActionClient.run(dataType, com.ismartcoding.plain.features.media.actions.MediaAction.RESTORE, ids)
}

/**
 * Permanently delete media items of [dataType] identified by [ids] from disk and MediaStore.
 * If [fromTrash] is true, only items already in the trash are deleted.
 */
suspend fun deleteMedia(dataType: DataType, ids: Set<String>, fromTrash: Boolean) {
    com.ismartcoding.plain.features.media.actions.MediaActionClient.run(dataType, com.ismartcoding.plain.features.media.actions.MediaAction.DELETE, ids, fromTrash)
}

/**
 * Move media files of [dataType] identified by [ids] into the directory [destDir].
 * Returns true when all items were moved successfully.
 */
suspend fun moveMedia(dataType: DataType, ids: Set<String>, destDir: String): Boolean =
    com.ismartcoding.plain.features.media.actions.MediaActionClient.run(dataType, com.ismartcoding.plain.features.media.actions.MediaAction.MOVE, ids, destDir = destDir) == ids.size

expect fun getMediaItemUriString(dataType: DataType, id: String, path: String): String


expect suspend fun deleteSmsRows(ids: List<String>): com.ismartcoding.plain.features.telephony.sms.SmsDeletionReceipt

expect fun divideSmsText(body: String, subscriptionId: Int?): List<String>
expect fun sendSmsParts(request: com.ismartcoding.plain.features.telephony.sms.SmsDispatchRequest)

/**
 * Initiate a phone call to [number].
 * If [showDialer] is true, opens the dialer with the number pre-filled.
 * Otherwise, directly places the call.
 */
expect fun call(number: String, showDialer: Boolean)


/**
 * Launch the platform's default SMS/MMS app with the supplied [number], [body]
 * and pre-resolved attachments (path, mimeType) pairs. Returns the launch time
 * in seconds since epoch (used to track delivery via polling).
 */
expect fun launchDefaultSmsApp(
    number: String,
    body: String,
    attachments: List<Pair<String, String>>,
): Long

/** Returns the newest sent MMS provider row ID, or zero when unavailable. */
expect fun getLatestSentMmsId(): Long

/**
 * Returns the screen size in pixels as a [Pair] of (width, height) on supported
 * platforms, or (0, 0) on platforms without a screen mirror surface.
 */
expect fun getScreenSize(): Pair<Int, Int>

/**
 * Returns the absolute path of the platform's public Downloads directory.
 * Empty string on unsupported platforms.
 */
fun getDownloadsDirPath(): String = downloadsDirectory

private val downloadsDirectory: String by lazy { platformDownloadsDirPath() }

internal expect fun platformDownloadsDirPath(): String

/**
 * Update the contact identified by [id] with the values in [input].
 * No-op on platforms without a contacts provider.
 */
expect fun updateContact(id: String, input: com.ismartcoding.plain.features.contacts.ContactInput)

/**
 * Create a new contact with the values in [input]. Returns the new contact's id,
 * or empty string on failure / unsupported platforms.
 */
expect fun createContact(input: com.ismartcoding.plain.features.contacts.ContactInput, accountType: String): String

/**
 * Delete the contacts identified by [ids]. Used by tag-cleanup flow.
 */
expect suspend fun deleteContacts(ids: Set<String>)

expect fun readSentMmsCandidates(minimumId: Long, launchTimeSec: Long): List<com.ismartcoding.plain.features.telephony.sms.MmsCandidateFacts>




expect suspend fun searchMedia(dataType: DataType, plan: ProviderPlan, limit: Int, offset: Int, sortBy: FileSortBy): List<IData>
expect suspend fun countMedia(dataType: DataType, plan: ProviderPlan): Int
expect suspend fun getMediaIds(dataType: DataType, plan: ProviderPlan): Set<String>

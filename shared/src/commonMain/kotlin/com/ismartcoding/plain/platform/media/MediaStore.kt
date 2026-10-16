package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.api.graphql.models.Image as MediaImage
import com.ismartcoding.plain.db.IData
import com.ismartcoding.plain.rust.host.provider.ProviderPlan
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.features.files.FileSortBy

/**
 * Move media items of [dataType] identified by [ids] to the trash.
 */
expect suspend fun trashMedia(dataType: DataType, ids: Set<String>)

/**
 * Restore media items of [dataType] identified by [ids] from the trash.
 */
expect suspend fun restoreMedia(dataType: DataType, ids: Set<String>)

/**
 * Permanently delete media items of [dataType] identified by [ids] from disk and MediaStore.
 * If [fromTrash] is true, only items already in the trash are deleted.
 */
expect suspend fun deleteMedia(dataType: DataType, ids: Set<String>, fromTrash: Boolean)

/**
 * Move media files of [dataType] identified by [ids] into the directory [destDir].
 * Returns true when all items were moved successfully. Returns false on
 * platforms without a media store (iOS).
 */
expect suspend fun moveMedia(dataType: DataType, ids: Set<String>, destDir: String): Boolean

/**
 * Combined image search: filename + optional AI semantic search.
 * If the AI model is not ready or [queryText] is blank, falls back to filename-only search.
 */
suspend fun searchImagesCombined(
    queryText: String,
    extraQuery: String,
    limit: Int,
    offset: Int,
    sortBy: FileSortBy,
): List<MediaImage> = com.ismartcoding.plain.features.images.search.RustImageSearch.rows(queryText, extraQuery, limit, offset, sortBy)

/**
 * Whether the on-device AI image search model is loaded and ready.
 */
fun isImageSearchModelReady(): Boolean = com.ismartcoding.plain.features.images.search.ImageSearchManager.isModelReady()

/**
 * Returns a URI string for the media item identified by [id] of [dataType],
 * suitable for sharing via the system share sheet. Empty string on failure.
 */
expect fun getMediaItemUriString(dataType: DataType, id: String): String

/**
 * Returns the file paths for the media items of [dataType] identified by [ids].
 * Used to clean up playlist entries when items are trashed.
 */
expect suspend fun getMediaPathsByIds(dataType: DataType, ids: Set<String>): Set<String>

/**
 * Count images matching a combined filename + AI semantic search [queryText]
 * with the additional [extraQuery] filter. Returns 0 on unsupported platforms
 * or when the AI model is not ready.
 */
suspend fun countImagesCombined(queryText: String, extraQuery: String): Int = com.ismartcoding.plain.features.images.search.RustImageSearch.count(queryText, extraQuery)

/**
 * Start a full scan of all images to (re)build the AI search index.
 * If [force] is true, re-index even if already indexed.
 */
fun startImageIndexFullScan(force: Boolean) = com.ismartcoding.plain.features.images.index.ImageIndexHelper.startAsync(force)

/**
 * Cancel any in-progress image indexing operation.
 */
fun cancelImageIndex() = com.ismartcoding.plain.features.images.index.ImageIndexHelper.cancelAsync()

/**
 * Build a snapshot of the current image-search status (model state, indexing progress).
 * Returns a default "unavailable" status on platforms without AI image search.
 */

/**
 * Look up geographic information (province, city, ISP) for a phone [number].
 * Returns null if no match is found or the platform has no lookup capability.
 */

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
 * Returns the MIME type for a file extension (e.g. "image/png").
 * Returns "application/octet-stream" if unknown.
 */
expect fun mimeTypeFromExtension(extension: String): String

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
expect fun createContact(input: com.ismartcoding.plain.features.contacts.ContactInput): String

/**
 * Delete the contacts identified by [ids]. Used by tag-cleanup flow.
 */
expect suspend fun deleteContacts(ids: Set<String>)

expect fun readSentMmsCandidates(minimumId: Long, launchTimeSec: Long): List<com.ismartcoding.plain.features.telephony.sms.MmsCandidateFacts>

/**
 * Enable the on-device AI image search model (downloads model if needed).
 */
suspend fun enableImageSearchAsync() = com.ismartcoding.plain.features.images.search.ImageSearchManager.enableAsync()

/**
 * Disable the on-device AI image search model and release resources.
 */
suspend fun disableImageSearchAsync() = com.ismartcoding.plain.features.images.search.ImageSearchManager.disableAsync()

/**
 * Cancel any in-progress download of the AI image search model.
 */
fun cancelImageModelDownload() = com.ismartcoding.plain.features.images.search.ImageSearchManager.cancelDownload()

expect suspend fun searchMedia(dataType: DataType, plan: ProviderPlan, limit: Int, offset: Int, sortBy: FileSortBy): List<IData>
expect suspend fun countMedia(dataType: DataType, plan: ProviderPlan): Int
expect suspend fun getMediaIds(dataType: DataType, plan: ProviderPlan): Set<String>

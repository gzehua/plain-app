package com.ismartcoding.plain.platform

import com.ismartcoding.plain.graphql.models.Image as MediaImage
import com.ismartcoding.plain.graphql.models.MediaBucket
import com.ismartcoding.plain.db.IData
import com.ismartcoding.plain.data.TagRelationStub
import com.ismartcoding.plain.features.system.ProviderPlan
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.features.file.FileSortBy

/**
 * Returns all media buckets (folders) for the given [dataType].
 */
suspend fun getMediaBuckets(dataType: DataType): List<MediaBucket> = com.ismartcoding.plain.features.system.RustSystemProviders.mediaBuckets(dataType)

/**
 * Search media items of [dataType] matching [query], paginated by [limit]/[offset]
 * and sorted by [sortBy]. Returns an empty list on unsupported platforms.
 */
suspend fun searchMedia(dataType: DataType, query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<IData> = when (dataType) {
    DataType.AUDIO, DataType.VIDEO, DataType.IMAGE, DataType.DOC -> com.ismartcoding.plain.features.system.RustMediaQuery.rows(dataType, query, limit, offset, sortBy)
    DataType.CALL -> com.ismartcoding.plain.features.system.RustPhoneQuery.calls(query, limit, offset)
    DataType.CONTACT -> com.ismartcoding.plain.features.system.RustPhoneQuery.contacts(query, limit, offset)
    DataType.SMS -> com.ismartcoding.plain.features.sms.RustSmsQuery.search(query, limit, offset)
    else -> error("Unsupported provider: $dataType")
}

/**
 * Count media items of [dataType] matching [query]. Returns 0 on unsupported platforms.
 */
suspend fun countMedia(dataType: DataType, query: String): Int = when (dataType) {
    DataType.AUDIO, DataType.VIDEO, DataType.IMAGE, DataType.DOC -> com.ismartcoding.plain.features.system.RustMediaQuery.count(dataType, query)
    DataType.CALL -> com.ismartcoding.plain.features.system.RustPhoneQuery.countCalls(query)
    DataType.CONTACT -> com.ismartcoding.plain.features.system.RustPhoneQuery.countContacts(query)
    DataType.SMS -> com.ismartcoding.plain.features.sms.RustSmsQuery.count(query)
    else -> error("Unsupported provider: $dataType")
}

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
 * Returns file-extension groups for documents matching [query].
 * Each pair is (extension, count). Returns an empty list on unsupported platforms.
 */
suspend fun getDocExtGroups(query: String): List<Pair<String, Int>> = com.ismartcoding.plain.features.system.RustMediaQuery.docExtGroups(query)

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
): List<MediaImage> = com.ismartcoding.plain.ai.RustImageSearch.rows(queryText, extraQuery, limit, offset, sortBy)

/**
 * Whether the on-device AI image search model is loaded and ready.
 */
fun isImageSearchModelReady(): Boolean = com.ismartcoding.plain.ai.ImageSearchManager.isModelReady()


/**
 * Returns a URI string for the media item identified by [id] of [dataType],
 * suitable for sharing via the system share sheet. Empty string on failure.
 */
expect fun getMediaItemUriString(dataType: DataType, id: String): String

/**
 * Returns the IDs of media items of [dataType] matching [query].
 * Used by tag operations and trash/delete mutations.
 */
suspend fun getMediaIds(dataType: DataType, query: String): Set<String> = when (dataType) {
    DataType.AUDIO, DataType.VIDEO, DataType.IMAGE, DataType.DOC -> com.ismartcoding.plain.features.system.RustMediaQuery.ids(dataType, query)
    DataType.CALL -> com.ismartcoding.plain.features.system.RustPhoneQuery.callIds(query)
    DataType.CONTACT -> com.ismartcoding.plain.features.system.RustPhoneQuery.contactIds(query)
    DataType.SMS -> com.ismartcoding.plain.features.sms.RustSmsQuery.ids(query)
    else -> error("Unsupported provider: $dataType")
}

/**
 * Returns the IDs of trashed media items of [dataType] matching [query].
 * On platforms without a trash concept, returns the same as [getMediaIds].
 */
suspend fun getTrashedMediaIds(dataType: DataType, query: String): Set<String> = com.ismartcoding.plain.features.system.RustMediaQuery.ids(dataType, query, fromTrash = true)

/**
 * Returns the file paths for the media items of [dataType] identified by [ids].
 * Used to clean up playlist entries when items are trashed.
 */
expect suspend fun getMediaPathsByIds(dataType: DataType, ids: Set<String>): Set<String>

/**
 * Returns [TagRelationStub]s for media items of [dataType] matching [query].
 * Used by tag-batch-add operations.
 */
suspend fun getMediaTagRelationStubs(dataType: DataType, query: String): List<TagRelationStub> = when (dataType) {
    DataType.AUDIO, DataType.VIDEO, DataType.IMAGE, DataType.DOC -> com.ismartcoding.plain.features.system.RustMediaQuery.tagStubs(dataType, query)
    else -> getMediaIds(dataType, query).map { TagRelationStub(it) }
}

/**
 * Count images matching a combined filename + AI semantic search [queryText]
 * with the additional [extraQuery] filter. Returns 0 on unsupported platforms
 * or when the AI model is not ready.
 */
suspend fun countImagesCombined(queryText: String, extraQuery: String): Int = com.ismartcoding.plain.ai.RustImageSearch.count(queryText, extraQuery)

/**
 * Start a full scan of all images to (re)build the AI search index.
 * If [force] is true, re-index even if already indexed.
 */
fun startImageIndexFullScan(force: Boolean) = com.ismartcoding.plain.features.imageindex.ImageIndexHelper.startAsync(force)

/**
 * Cancel any in-progress image indexing operation.
 */
fun cancelImageIndex() = com.ismartcoding.plain.features.imageindex.ImageIndexHelper.cancelAsync()

/**
 * Build a snapshot of the current image-search status (model state, indexing progress).
 * Returns a default "unavailable" status on platforms without AI image search.
 */

/**
 * Look up geographic information (province, city, ISP) for a phone [number].
 * Returns null if no match is found or the platform has no lookup capability.
 */

expect suspend fun deleteSmsRows(ids: List<String>): com.ismartcoding.plain.features.sms.SmsDeletionReceipt

expect fun divideSmsText(body: String, subscriptionId: Int?): List<String>
expect fun sendSmsParts(request: com.ismartcoding.plain.features.sms.SmsDispatchRequest)

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
expect fun updateContact(id: String, input: com.ismartcoding.plain.features.contact.ContactInput)

/**
 * Create a new contact with the values in [input]. Returns the new contact's id,
 * or empty string on failure / unsupported platforms.
 */
expect fun createContact(input: com.ismartcoding.plain.features.contact.ContactInput): String

/**
 * Delete the contacts identified by [ids]. Used by tag-cleanup flow.
 */
expect suspend fun deleteContacts(ids: Set<String>)

expect fun readSentMmsCandidates(minimumId: Long, launchTimeSec: Long): List<com.ismartcoding.plain.features.sms.MmsCandidateFacts>

/**
 * Enable the on-device AI image search model (downloads model if needed).
 */
suspend fun enableImageSearchAsync() = com.ismartcoding.plain.ai.ImageSearchManager.enableAsync()

/**
 * Disable the on-device AI image search model and release resources.
 */
suspend fun disableImageSearchAsync() = com.ismartcoding.plain.ai.ImageSearchManager.disableAsync()

/**
 * Cancel any in-progress download of the AI image search model.
 */
fun cancelImageModelDownload() = com.ismartcoding.plain.ai.ImageSearchManager.cancelDownload()

expect suspend fun searchMedia(dataType: DataType, plan: ProviderPlan, limit: Int, offset: Int, sortBy: FileSortBy): List<IData>
expect suspend fun countMedia(dataType: DataType, plan: ProviderPlan): Int
expect suspend fun getMediaIds(dataType: DataType, plan: ProviderPlan): Set<String>

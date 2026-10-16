package com.ismartcoding.plain.platform.files

import com.ismartcoding.plain.api.graphql.models.File as FileEntry

import com.ismartcoding.plain.features.files.FilesClient
import com.ismartcoding.plain.features.files.DStorageStatsItem
import com.ismartcoding.plain.features.files.FileSortBy

/**
 * Path to the primary internal storage (e.g. /storage/emulated/0).
 * Empty string on iOS.
 */
expect fun getInternalStoragePath(): String

/**
 * Display name for internal storage.
 */
expect fun getInternalStorageName(): String

/**
 * Path to the SD card, or empty string if none.
 */
expect fun getSDCardPath(): String

/**
 * Paths to USB storage devices.
 */
expect fun getUsbDiskPaths(): List<String>

/**
 * List files in [dir], optionally including hidden files, sorted by [sortBy].
 */
suspend fun listFilesInDir(dir: String, showHidden: Boolean, sortBy: FileSortBy): List<FileEntry> = FilesClient.list(dir, showHidden, sortBy)

/**
 * Search files recursively under [root] matching [query], optionally including hidden files.
 * Rust owns query parsing, traversal, filtering and sorting.
 */
suspend fun searchFilesInDir(query: String, root: String, sortBy: FileSortBy): List<FileEntry> = FilesClient.search(query, root, sortBy)

/**
 * Recursively search files under [dir] whose name contains [query] (case-insensitive).
 * When [showHidden] is false, hidden files are skipped. Results are sorted by [sortBy].
 */
suspend fun searchFilesByName(query: String, dir: String, showHidden: Boolean, sortBy: FileSortBy): List<FileEntry> = FilesClient.searchName(query, dir, showHidden, sortBy)

suspend fun searchFiles(query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<FileEntry> = FilesClient.indexed(query, limit, offset, sortBy)

suspend fun countFiles(query: String): Int = FilesClient.indexedCount(query)

/**
 * Returns the most recently modified files (up to 100) from MediaStore.
 */
suspend fun getRecentFiles(): List<FileEntry> = FilesClient.recent()

/**
 * Create a directory at [path] (including parents). Returns the resulting FileEntry.
 */
suspend fun createDirectory(path: String): FileEntry = FilesClient.createDirectory(path)

/**
 * Create an empty file at [path]. Returns the resulting FileEntry.
 */
suspend fun createFile(path: String): FileEntry = FilesClient.createFile(path)

/**
 * Notify the media scanner that files at [paths] were added/removed.
 * No-op on iOS.
 */
expect fun scanFiles(paths: Array<String>)

/**
 * Rename a file from [path] to [newName] and notify the media scanner.
 * Returns the new absolute path on success, null on failure.
 */
suspend fun renameAndScanFile(path: String, newName: String): String? = FilesClient.rename(path, newName)

/**
 * Storage stats for internal storage.
 */
expect fun getInternalStorageStats(): DStorageStatsItem

/**
 * Storage stats for the SD card.
 */
expect fun getSDCardStorageStats(): DStorageStatsItem

/**
 * Storage stats for each USB storage device.
 */
expect fun getUSBStorageStats(): List<DStorageStatsItem>

/**
 * List entries inside a zip file at [zipVirtualPath], sorted by [sortBy].
 * Returns empty list if the path is not a zip or cannot be read.
 */
expect suspend fun listZipEntries(zipVirtualPath: String, sortBy: FileSortBy): List<FileEntry>

/**
 * Extract a single zip entry to a temporary cache file and return its path.
 * Returns null if extraction fails.
 */
suspend fun extractZipEntryToCache(zipVirtualPath: String): String? = try {
    com.ismartcoding.plain.features.files.FilesClient.cacheZipEntry(zipVirtualPath)
} catch (cancelled: kotlinx.coroutines.CancellationException) {
    throw cancelled
} catch (_: Exception) {
    null
}

/**
 * Delete a file or directory (recursively) at [path]. Returns true on success.
 */
suspend fun deleteFileOrDir(path: String): Boolean = FilesClient.delete(path)


/**
 * Canonical absolute path of [path] (resolving symlinks). Falls back to the
 * input path on platforms without a canonical notion.
 */
expect fun getCanonicalPath(path: String): String



/**
 * Zip the given [sourcePaths] (files and/or directories) into a new archive at
 * [targetPath]. Returns true on success.
 */
suspend fun zipFiles(sourcePaths: List<String>, targetPath: String): Boolean = archiveOperation {
    com.ismartcoding.plain.features.files.FilesClient.createZipArchive(sourcePaths, targetPath)
}

/**
 * Unzip the archive at [zipPath] into the directory [destPath] (created if needed).
 * Returns true on success.
 */
suspend fun unzipFile(zipPath: String, destPath: String): Boolean = archiveOperation {
    com.ismartcoding.plain.features.files.FilesClient.extractZipArchive(zipPath, destPath)
}

private suspend fun archiveOperation(operation: suspend () -> String): Boolean = try {
    operation()
    true
} catch (cancelled: kotlinx.coroutines.CancellationException) {
    throw cancelled
} catch (_: Exception) {
    false
}

/**
 * Returns file metadata (size, last-modified, is-directory) for [path], or null
 * if the file does not exist. Children count is 0 for non-directories.
 */
expect fun statFile(path: String): FileEntry?

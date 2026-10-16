package com.ismartcoding.plain.platform

import com.ismartcoding.plain.features.file.extension

import com.ismartcoding.plain.graphql.models.File as FileEntry

import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.extensions.sorted
import com.ismartcoding.plain.features.file.DStorageStatsItem
import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.features.file.FileSystemHelper
import com.ismartcoding.plain.extensions.newPath
import com.ismartcoding.plain.helpers.FileHelper
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.lib.extensions.scanFileByConnection
import com.ismartcoding.plain.lib.logcat.LogCat
import java.io.File
import kotlin.io.path.moveTo
import kotlin.time.Instant

actual fun getInternalStoragePath(): String = FileSystemHelper.getInternalStoragePath()

actual fun getInternalStorageName(): String = FileSystemHelper.getInternalStorageName()

actual fun getSDCardPath(): String = FileSystemHelper.getSDCardPath(appContext)

actual fun getUsbDiskPaths(): List<String> = FileSystemHelper.getUsbDiskPaths()

actual fun scanFiles(paths: Array<String>) {
    appContext.scanFileByConnection(paths)
}

actual fun getInternalStorageStats(): DStorageStatsItem = FileSystemHelper.getInternalStorageStats()

actual fun getSDCardStorageStats(): DStorageStatsItem = FileSystemHelper.getSDCardStorageStats(appContext)

actual fun getUSBStorageStats(): List<DStorageStatsItem> = FileSystemHelper.getUSBStorageStats()

actual suspend fun listZipEntries(zipVirtualPath: String, sortBy: FileSortBy): List<FileEntry> =
    com.ismartcoding.plain.features.file.RustFileHelper.list(zipVirtualPath, true, sortBy)


actual fun getCanonicalPath(path: String): String =
    runCatching { File(path).canonicalPath }.getOrDefault(path)



actual fun statFile(path: String): FileEntry? {
    val file = File(path)
    if (!file.exists()) return null
    return FileEntry(name = file.name, path = file.absolutePath, permission = "", createdAt = null, updatedAt = Instant.fromEpochMilliseconds(file.lastModified()), size = if (file.isDirectory) 0L else file.length(), isDir = file.isDirectory, childCount = 0, mediaId = "")
}

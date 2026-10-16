@file:OptIn(ExperimentalForeignApi::class)

package com.ismartcoding.plain.platform


import com.ismartcoding.plain.graphql.models.File as FileEntry

import com.ismartcoding.plain.features.file.DStorageStatsItem
import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.lib.logcat.LogCat
import kotlin.time.Instant
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.datetime.toKotlinInstant
import platform.Foundation.NSDate
import platform.Foundation.NSFileModificationDate
import platform.Foundation.NSFileSystemFreeSize
import platform.Foundation.NSFileSystemSize
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSFileType
import platform.Foundation.NSFileTypeDirectory

actual fun getInternalStoragePath(): String = appDir()

actual fun getInternalStorageName(): String = "Internal Storage"

actual fun getSDCardPath(): String = ""

actual fun getUsbDiskPaths(): List<String> = emptyList()


actual fun scanFiles(paths: Array<String>) {}

actual fun getInternalStorageStats(): DStorageStatsItem {
    val path = appDir()
    return try {
        val attrs = NSFileManager.defaultManager.attributesOfFileSystemForPath(path, null)
            ?: return DStorageStatsItem(0, 0)
        val total = (attrs[NSFileSystemSize] as? Long) ?: 0L
        val free = (attrs[NSFileSystemFreeSize] as? Long) ?: 0L
        DStorageStatsItem(total, free)
    } catch (e: Exception) {
        DStorageStatsItem(0, 0)
    }
}

actual fun getSDCardStorageStats(): DStorageStatsItem = DStorageStatsItem(0, 0)

actual fun getUSBStorageStats(): List<DStorageStatsItem> = emptyList()

actual suspend fun listZipEntries(zipVirtualPath: String, sortBy: FileSortBy): List<FileEntry> = com.ismartcoding.plain.features.file.RustFileHelper.list(zipVirtualPath, true, sortBy)


actual fun getCanonicalPath(path: String): String {
    val resolved = platform.posix.realpath(path, null) ?: return path
    return try { resolved.toKString() } finally { platform.posix.free(resolved) }
}



actual fun statFile(path: String): FileEntry? {
    if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return null
    val name = path.substringAfterLast('/')
    return buildFile(path, name)
}

private fun buildFile(path: String, name: String): FileEntry? {
    val mgr = NSFileManager.defaultManager
    val attrs = try {
        mgr.attributesOfItemAtPath(path, null) ?: return null
    } catch (e: Exception) {
        return null
    }
    val isDir = attrs[NSFileType] == NSFileTypeDirectory
    val size = if (isDir) 0L else (attrs[NSFileSize] as? Long) ?: 0L
    val updatedAt = (attrs[NSFileModificationDate] as? NSDate?)?.toKotlinInstant()
        ?: Instant.fromEpochMilliseconds(0)
    return FileEntry(name = name, path = path, permission = "", createdAt = null, updatedAt = updatedAt, size = size, isDir = isDir, childCount = 0, mediaId = "")
}

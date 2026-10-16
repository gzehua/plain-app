@file:OptIn(ExperimentalForeignApi::class)

package com.ismartcoding.plain.platform.files

import com.ismartcoding.plain.platform.system.appDir

import com.ismartcoding.plain.features.files.DStorageStatsItem
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.Foundation.NSFileSystemFreeSize
import platform.Foundation.NSFileSystemSize
import platform.Foundation.NSFileManager

actual fun getInternalStoragePath(): String = appDir()

actual fun getInternalStorageName(): String = "Internal Storage"

actual fun getSDCardPath(): String = ""

actual fun getUsbDiskPaths(): List<String> = emptyList()

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

actual fun getCanonicalPath(path: String): String {
    val resolved = platform.posix.realpath(path, null) ?: return path
    return try { resolved.toKString() } finally { platform.posix.free(resolved) }
}

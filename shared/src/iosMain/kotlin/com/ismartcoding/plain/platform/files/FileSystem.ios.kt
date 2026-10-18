@file:OptIn(ExperimentalForeignApi::class)

package com.ismartcoding.plain.platform.files

import com.ismartcoding.plain.platform.system.appDir

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileSystemFreeSize
import platform.Foundation.NSFileSystemSize
import platform.Foundation.NSFileManager

actual fun getInternalStoragePath(): String = appDir()

actual fun getInternalStorageName(): String = "Internal Storage"

actual fun getSDCardPath(): String = ""

actual fun getUsbDiskPaths(): List<String> = emptyList()

actual fun getInternalStorageStats(): StorageStats {
    val path = appDir()
    return try {
        val attrs = NSFileManager.defaultManager.attributesOfFileSystemForPath(path, null)
            ?: return StorageStats(0, 0)
        val total = (attrs[NSFileSystemSize] as? Long) ?: 0L
        val free = (attrs[NSFileSystemFreeSize] as? Long) ?: 0L
        StorageStats(total, free)
    } catch (e: Exception) {
        StorageStats(0, 0)
    }
}

actual fun getSDCardStorageStats(): StorageStats = StorageStats(0, 0)

actual fun getUSBStorageStats(): List<StorageStats> = emptyList()

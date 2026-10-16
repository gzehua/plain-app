package com.ismartcoding.plain.platform.files

import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.features.files.DStorageStatsItem
import com.ismartcoding.plain.features.files.FileSystemHelper
import java.io.File

actual fun getInternalStoragePath(): String = FileSystemHelper.getInternalStoragePath()

actual fun getInternalStorageName(): String = FileSystemHelper.getInternalStorageName()

actual fun getSDCardPath(): String = FileSystemHelper.getSDCardPath(appContext)

actual fun getUsbDiskPaths(): List<String> = FileSystemHelper.getUsbDiskPaths()

actual fun getInternalStorageStats(): DStorageStatsItem = FileSystemHelper.getInternalStorageStats()

actual fun getSDCardStorageStats(): DStorageStatsItem = FileSystemHelper.getSDCardStorageStats(appContext)

actual fun getUSBStorageStats(): List<DStorageStatsItem> = FileSystemHelper.getUSBStorageStats()

actual fun getCanonicalPath(path: String): String =
    runCatching { File(path).canonicalPath }.getOrDefault(path)

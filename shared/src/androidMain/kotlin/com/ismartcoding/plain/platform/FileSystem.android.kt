package com.ismartcoding.plain.platform

import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.extensions.sorted
import com.ismartcoding.plain.features.file.DFile
import com.ismartcoding.plain.features.file.DStorageStatsItem
import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.features.file.FileSystemHelper
import com.ismartcoding.plain.extensions.newPath
import com.ismartcoding.plain.features.media.FileMediaStoreHelper
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

actual suspend fun getRecentFiles(): List<DFile> = withIO {
    if (isQPlus()) {
        FileMediaStoreHelper.getRecentFilesAsync(appContext)
    } else {
        FileSystemHelper.getRecentFiles().map { file ->
            DFile(
                name = file.name,
                path = file.absolutePath,
                permission = "",
                createdAt = null,
                updatedAt = Instant.fromEpochMilliseconds(file.lastModified()),
                size = file.length(),
                isDir = false,
                childCount = 0,
                mediaId = "",
            )
        }
    }
}

actual fun scanFiles(paths: Array<String>) {
    appContext.scanFileByConnection(paths)
}

actual fun getInternalStorageStats(): DStorageStatsItem = FileSystemHelper.getInternalStorageStats()

actual fun getSDCardStorageStats(): DStorageStatsItem = FileSystemHelper.getSDCardStorageStats(appContext)

actual fun getUSBStorageStats(): List<DStorageStatsItem> = FileSystemHelper.getUSBStorageStats()

actual suspend fun listZipEntries(zipVirtualPath: String, sortBy: FileSortBy): List<DFile> =
    com.ismartcoding.plain.features.file.RustFileHelper.list(zipVirtualPath, true, sortBy)

actual fun getNewPath(path: String): String = File(path).newPath()

actual fun getCanonicalPath(path: String): String =
    runCatching { File(path).canonicalPath }.getOrDefault(path)

actual fun copyFileOrDir(srcPath: String, destPath: String): Boolean {
    val src = File(srcPath)
    val dest = File(destPath)
    return if (src.isDirectory) src.copyRecursively(dest, true)
    else src.copyTo(dest, true).let { true }
}

actual fun moveFileOrDir(srcPath: String, destPath: String): Boolean {
    val src = File(srcPath)
    val dest = File(destPath)
    return runCatching {
        if (!dest.exists()) {
            src.toPath().moveTo(dest.toPath(), true)
        } else {
            src.toPath().moveTo(kotlin.io.path.Path(dest.newPath()), true)
        }
        true
    }.getOrElse {
        val target = if (!dest.exists()) dest else File(dest.newPath())
        val ok = if (src.isDirectory) src.copyRecursively(target, true)
        else src.copyTo(target, true).let { true }
        if (ok) src.deleteRecursively() else false
    }
}

actual fun statFile(path: String): DFile? {
    val file = File(path)
    if (!file.exists()) return null
    return DFile(
        name = file.name,
        path = file.absolutePath,
        permission = "",
        createdAt = null,
        updatedAt = Instant.fromEpochMilliseconds(file.lastModified()),
        size = if (file.isDirectory) 0L else file.length(),
        isDir = file.isDirectory,
        childCount = 0,
        mediaId = "",
    )
}

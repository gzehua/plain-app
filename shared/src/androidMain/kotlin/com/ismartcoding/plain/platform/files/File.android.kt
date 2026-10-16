package com.ismartcoding.plain.platform.files

import com.ismartcoding.plain.rust.http.copyTo

import com.ismartcoding.plain.features.files.extension

import com.ismartcoding.plain.api.graphql.models.File as FileEntry

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.app.AppHelper
import com.ismartcoding.plain.platform.files.FileHelper
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.lib.extensions.getFilenameFromPath
import com.ismartcoding.plain.lib.extensions.isHeifHeader
import com.ismartcoding.plain.lib.extensions.queryOpenableFile
import com.ismartcoding.plain.lib.extensions.queryOpenableFileName
import com.ismartcoding.plain.platform.files.StreamSink
import android.net.Uri
import androidx.core.net.toUri
import com.ismartcoding.plain.features.PackageHelper
import com.ismartcoding.plain.platform.media.provider.FileMediaStoreHelper
import com.ismartcoding.plain.lib.extensions.compress
import com.ismartcoding.plain.ui.media.thumbnail.DecodeLimiter
import com.ismartcoding.plain.thumbnail.DecodePolicy
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.RandomAccessFile

actual fun deleteFileAt(path: String) {
    File(path).delete()
}

actual fun fileSize(path: String): Long = File(path).takeIf { it.isFile }?.length() ?: 0L

actual fun copyFile(srcPath: String, destPath: String): Boolean {
    return try {
        File(srcPath).copyTo(File(destPath), overwrite = true)
        true
    } catch (_: Exception) {
        false
    }
}

actual fun writeBytesToPath(path: String, bytes: ByteArray): Boolean {
    return try {
        File(path).writeBytes(bytes)
        true
    } catch (_: Exception) {
        false
    }
}

actual suspend fun saveFileToDownloads(path: String, fileName: String): String = withIO {
    FileHelper.copyFileToDownloads(path, fileName)
}

actual fun fileToUriString(path: String): String = File(path).toUri().toString()

actual fun getFileIconPath(extension: String): String =
    AppHelper.getFileIconPath(extension)

actual fun fileExists(path: String): Boolean = File(path).exists()

actual suspend fun readFileRange(path: String, offset: Long, length: Int): ByteArray? = withIO {
    try {
        val file = File(path)
        if (!file.exists() || !file.isFile) return@withIO null
        val fileLen = file.length()
        if (offset >= fileLen) return@withIO ByteArray(0)
        val readLen = minOf(length.toLong(), fileLen - offset).toInt()
        if (readLen <= 0) return@withIO ByteArray(0)
        val bytes = ByteArray(readLen)
        RandomAccessFile(file, "r").use { raf ->
            raf.seek(offset)
            raf.readFully(bytes)
        }
        bytes
    } catch (e: Exception) {
        null
    }
}

actual suspend fun ensureParentDir(path: String) {
    withIO {
        File(path).parentFile?.mkdirs()
    }
}

actual suspend fun streamContentUri(uri: String, sink: StreamSink): String? = withIO {
    val context = appContext
    val parsed = Uri.parse(uri)
    val mimeType = context.contentResolver.getType(parsed).orEmpty()
    try {
        context.contentResolver.openInputStream(parsed)?.buffered()?.use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                sink.write(buffer, 0, read)
            }
        }
        mimeType.ifEmpty { null }
    } catch (e: Exception) {
        null
    }
}

actual suspend fun getPackageIconBytes(packageName: String): ByteArray? = withIO {
    val bitmap = PackageHelper.getIcon(packageName)
    ByteArrayOutputStream().use {
        bitmap.compress(80, it)
        it.toByteArray()
    }
}

actual suspend fun decodeImageFileToPng(path: String): ByteArray? = withIO {
    val header = ByteArray(12)
    val headerSize = File(path).inputStream().use { it.read(header) }
    val isHeif = headerSize >= 12 && isHeifHeader(header)
    // Cheap header sniff stays outside the permit so non-HEIF requests
    // (e.g. every <video> playback fetch) never queue behind decodes.
    if (!isHeif) return@withIO null
    DecodeLimiter.withPermit {
        // Cap the decoded edge: a 48 MP HEIC would otherwise allocate ~192 MB
        // per open view, which is fatal on small-heap compatibility containers.
        // 4096 px keeps 12 MP photos pixel-exact.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withPermit null
        val opts = BitmapFactory.Options().apply {
            inSampleSize = DecodePolicy.capSampleSize(
                bounds.outWidth, bounds.outHeight, DecodePolicy.MAX_FULL_VIEW_EDGE
            )
        }
        val bitmap = BitmapFactory.decodeFile(path, opts) ?: return@withPermit null
        try {
            ByteArrayOutputStream().use { baos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                baos.toByteArray()
            }
        } finally {
            bitmap.recycle()
        }
    }
}

actual suspend fun decodeThumbnailBytes(
    path: String,
    width: Int,
    height: Int,
    centerCrop: Boolean,
    mediaId: String,
    fileName: String,
): ByteArray? = withIO {
    val file = File(path)
    if (!file.exists()) return@withIO null
    com.ismartcoding.plain.ui.media.thumbnail.ThumbnailGenerator.decodeBytesAsync(appContext, file, width, height, centerCrop, mediaId, fileName)
}

actual fun isContentUri(path: String): Boolean = path.startsWith("content://")

actual suspend fun readTextFile(path: String): String = withIO {
    try {
        if (path.startsWith("content://")) {
            val uri = Uri.parse(path)
            appContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: ""
        } else {
            val filePath = if (path.startsWith("file://")) Uri.parse(path).path else path
            File(filePath ?: path).readText()
        }
    } catch (e: Exception) {
        ""
    }
}

actual suspend fun writeBytesToUri(uriStr: String, bytes: ByteArray): Boolean = withIO {
    try {
        if (uriStr.startsWith("content://")) {
            val uri = Uri.parse(uriStr)
            appContext.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } != null
        } else {
            File(uriStr).writeBytes(bytes)
            true
        }
    } catch (_: Exception) {
        false
    }
}

actual fun getFileNameFromUri(uriStr: String): String? {
    if (!uriStr.startsWith("content://")) {
        return uriStr.getFilenameFromPath()
    }
    return try {
        appContext.contentResolver.queryOpenableFileName(Uri.parse(uriStr)).takeIf { it.isNotEmpty() }
    } catch (_: Exception) {
        null
    }
}

actual fun queryPickedFileInfo(uriStr: String): PickedFileInfo? {
    return try {
        val uri = Uri.parse(uriStr)
        val cr = appContext.contentResolver
        val file = cr.queryOpenableFile(uri) ?: return null
        val mime = cr.getType(uri) ?: ""
        PickedFileInfo(file.displayName, file.size, mime)
    } catch (_: Exception) {
        null
    }
}

actual suspend fun getFileByMediaId(mediaId: String): FileEntry? = withIO {
    if (mediaId.isEmpty()) null
    else FileMediaStoreHelper.getByIdAsync(appContext, mediaId)
}

internal actual suspend fun provideResourceFile(id: String, uri: String): Unit = withIO {
    appContext.contentResolver.openAssetFileDescriptor(Uri.parse(uri), "r")?.use { asset ->
        val error = com.ismartcoding.plain.rust.bridge.RustCoreBridge.resourceFile(
            id, asset.parcelFileDescriptor.fd, asset.startOffset, asset.declaredLength,
        )
        check(error.isEmpty()) { error }
    } ?: error("Unable to open content URI")
}

actual suspend fun copyFileToUri(path: String, uriStr: String): Boolean = withIO {
    val uri = Uri.parse(uriStr)
    val source = File(path)
    if (uri.scheme == "content") {
        val output = appContext.contentResolver.openOutputStream(uri, "wt") ?: return@withIO false
        output.use { sink -> source.inputStream().use { input -> input.copyTo(sink) } }
    } else {
        val destination = File(if (uri.scheme == "file") uri.path ?: return@withIO false else uriStr)
        if (source.canonicalFile == destination.canonicalFile) return@withIO false
        source.copyTo(destination, overwrite = true)
    }
    true
}

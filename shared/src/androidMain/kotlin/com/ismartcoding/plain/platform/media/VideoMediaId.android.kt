package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.platform.isQPlus
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.permissions.isGranted

import android.content.ContentUris
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.lib.withIO

actual suspend fun getVideoMediaId(path: String): String? = withIO {
    val uri = Uri.parse(path)
    if (uri.scheme == "content") {
        return@withIO when (uri.authority) {
            MediaStore.AUTHORITY -> if ("video" in uri.pathSegments) runCatching { ContentUris.parseId(uri).toString() }.getOrNull() else null
            "com.android.providers.media.documents" -> runCatching {
                DocumentsContract.getDocumentId(uri).takeIf { it.startsWith("video:") }?.substringAfter(':')?.toLongOrNull()?.toString()
            }.getOrNull()
            else -> null
        }
    }
    if (!Permission.WRITE_EXTERNAL_STORAGE.isGranted() && !Permission.READ_MEDIA_VIDEOS.isGranted()) return@withIO null
    val videoUri = if (isQPlus()) MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL) else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    val filePath = if (uri.scheme == "file") uri.path ?: return@withIO null else path
    try {
        appContext.contentResolver.query(videoUri, arrayOf(MediaStore.Video.Media._ID), "${MediaStore.Video.Media.DATA} = ?", arrayOf(filePath), null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getLong(0).toString() else null
        }
    } catch (_: SecurityException) { null }
}

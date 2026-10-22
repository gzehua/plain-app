package com.ismartcoding.plain.extensions


import android.database.Cursor
import android.provider.MediaStore
import com.ismartcoding.plain.lib.extensions.getIntValue
import com.ismartcoding.plain.lib.extensions.getLongValue
import com.ismartcoding.plain.lib.extensions.getStringValue
import com.ismartcoding.plain.lib.extensions.getTimeSecondsValue
import com.ismartcoding.plain.models.File as FileEntry

fun Cursor.toFile(cache: MutableMap<String, Int>): FileEntry {
    val id = getStringValue(MediaStore.Files.FileColumns._ID, cache)
    val title = getStringValue(MediaStore.Files.FileColumns.DISPLAY_NAME, cache)
    val size = getLongValue(MediaStore.Files.FileColumns.SIZE, cache)
    val path = getStringValue(MediaStore.Files.FileColumns.DATA, cache)
    val createdAt = getTimeSecondsValue(MediaStore.Files.FileColumns.DATE_ADDED, cache)
    val updatedAt = getTimeSecondsValue(MediaStore.Files.FileColumns.DATE_MODIFIED, cache)
    val mimeType =
        getString(getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))
    val mediaType = getIntValue(MediaStore.Files.FileColumns.MEDIA_TYPE, cache)
    return FileEntry(name = title, path = path, permission = "", createdAt = createdAt, updatedAt = updatedAt, size = size, isDir = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_NONE && (mimeType == null || mimeType == "vnd.android.document/directory"), childCount = 0, mediaId = id)
}
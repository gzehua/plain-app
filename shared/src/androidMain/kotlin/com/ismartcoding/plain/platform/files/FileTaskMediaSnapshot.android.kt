package com.ismartcoding.plain.platform.files

import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.permissions.isGranted

import android.provider.MediaStore
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.platform.media.MediaStoreFileRow

fun queryFileTaskMedia(paths: List<String>): List<MediaStoreFileRow> {
    if (paths.isEmpty()) return emptyList()
    require(paths.size <= 128)
    check(Permission.WRITE_EXTERNAL_STORAGE.isGranted()) { "Media identity permission denied" }
    val projection = arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.MEDIA_TYPE, MediaStore.Files.FileColumns.MIME_TYPE)
    val where = "${MediaStore.Files.FileColumns.DATA} IN (${paths.joinToString(",") { "?" }})"
    val rows = mutableListOf<MediaStoreFileRow>()
    checkNotNull(appContext.contentResolver.query(MediaStore.Files.getContentUri("external"), projection, where, paths.toTypedArray(), null)) { "Media identity provider unavailable" }.use { cursor ->
        while (cursor.moveToNext()) {
            val path = cursor.getString(1) ?: continue
            if (path in paths) rows.add(MediaStoreFileRow(cursor.getLong(0).toString(), path, cursor.getInt(2), cursor.getString(3)))
        }
    }
    return rows
}

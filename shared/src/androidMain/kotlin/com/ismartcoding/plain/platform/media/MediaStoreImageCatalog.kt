package com.ismartcoding.plain.platform.media

import android.database.ContentObserver
import android.os.Bundle
import android.provider.MediaStore
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.permissions.isGranted
import com.ismartcoding.plain.platform.isQPlus
import com.ismartcoding.plain.platform.isRPlus
import com.ismartcoding.plain.lib.extensions.paging
import com.ismartcoding.plain.lib.extensions.where
import kotlinx.serialization.json.*
import java.util.concurrent.atomic.AtomicLong
import android.content.ContentResolver

object MediaStoreImageCatalog : ImageCatalog {
    private val epoch = AtomicLong()
    private var observer: ContentObserver? = null
    private val uri get() = if (isQPlus()) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL) else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    private fun requirePermission() {
        check(Permission.WRITE_EXTERNAL_STORAGE.isGranted() || Permission.READ_MEDIA_IMAGES.isGranted()) { "Image catalog permission denied" }
    }
    @Synchronized
    private fun observe() {
        if (observer != null) return
        val value = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) { epoch.incrementAndGet() }
        }
        appContext.contentResolver.registerContentObserver(uri, true, value)
        observer = value
    }
    private fun currentRevision(): String {
        requirePermission()
        return if (isRPlus()) {
            checkNotNull(mediaStoreRevision()).joinToString("|") { volume ->
                "${volume.volume}:${volume.version}:${volume.generation}"
            } + "|${epoch.get()}"
        } else "${MediaStore.getVersion(appContext)}:${epoch.get()}"
    }
    override fun snapshot(): JsonObject {
        requirePermission()
        observe()
        val revision = currentRevision()
        val total = checkNotNull(appContext.contentResolver.query(uri,arrayOf(MediaStore.Images.Media._ID),null,null,null)) { "Image catalog query failed" }.use { it.count }
        verify(revision)
        return JsonHelper.jsonEncodeToElement(ImageCatalogSnapshot(revision,total)).jsonObject
    }
    override fun verify(revision: String) { check(currentRevision() == revision) { "Image catalog changed during indexing" } }
    override fun page(revision: String, cursor: String, limit: Int): JsonObject {
        require(limit in 1..128)
        verify(revision)
        val after = if (cursor.isEmpty()) -1L else cursor.toLong()
        val columns = arrayOf(MediaStore.Images.Media._ID,MediaStore.Images.Media.DATA)
        val selection = "${MediaStore.Images.Media._ID} > ?"
        val args = arrayOf(after.toString())
        val queried = if (isRPlus()) {
            appContext.contentResolver.query(uri,columns,Bundle().apply {
                paging(0,limit)
                where(selection,args.toList())
                putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS,arrayOf(MediaStore.Images.Media._ID))
                putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION,ContentResolver.QUERY_SORT_DIRECTION_ASCENDING)
            },null)
        } else appContext.contentResolver.query(uri,columns,selection,args,"${MediaStore.Images.Media._ID} ASC LIMIT $limit")
        var last = cursor
        val items = checkNotNull(queried) { "Image catalog page failed" }.use { rows ->
            val idColumn = rows.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val pathColumn = rows.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            buildList<ImageCatalogItem> {
                while (rows.moveToNext()) {
                    val id = rows.getLong(idColumn).toString()
                    val path = checkNotNull(rows.getString(pathColumn)) { "Image path unavailable" }
                    check(path.isNotEmpty()) { "Image path unavailable" }
                    add(ImageCatalogItem(id,path))
                    last = id
                }
            }
        }
        verify(revision)
        return JsonHelper.jsonEncodeToElement(ImageCatalogPage(revision,items,last,items.size < limit)).jsonObject
    }
    override fun resolve(revision: String, ids: List<String>): JsonArray {
        require(ids.size <= 100)
        verify(revision)
        if (ids.isEmpty()) return JsonArray(emptyList())
        val selection = "${MediaStore.Images.Media._ID} IN (${ids.joinToString(",") { "?" }})"
        val rows = checkNotNull(appContext.contentResolver.query(uri,arrayOf(MediaStore.Images.Media._ID,MediaStore.Images.Media.DATA),selection,ids.toTypedArray(),null)) { "Image selection query failed" }
        val items = rows.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            buildList<ImageCatalogItem> {
                while (cursor.moveToNext()) {
                    val path = checkNotNull(cursor.getString(pathColumn)) { "Image path unavailable" }
                    check(path.isNotEmpty()) { "Image path unavailable" }
                    add(ImageCatalogItem(cursor.getLong(idColumn).toString(),path))
                }
            }
        }
        verify(revision)
        return JsonHelper.jsonEncodeToElement(items).jsonArray
    }
    @Synchronized
    fun close() {
        observer?.let { appContext.contentResolver.unregisterContentObserver(it) }
        observer = null
        epoch.incrementAndGet()
    }
}

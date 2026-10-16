package com.ismartcoding.plain.features.media

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.BaseColumns
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.ismartcoding.plain.helpers.ContentWhere
import com.ismartcoding.plain.data.SortBy
import com.ismartcoding.plain.lib.extensions.count
import com.ismartcoding.plain.lib.extensions.forEach
import com.ismartcoding.plain.lib.extensions.getLongValue
import com.ismartcoding.plain.lib.extensions.getPagingCursor
import com.ismartcoding.plain.lib.extensions.getSearchCursor
import com.ismartcoding.plain.lib.extensions.getStringValue
import com.ismartcoding.plain.lib.extensions.map
import com.ismartcoding.plain.lib.extensions.scanFileByConnection
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.features.system.MediaBucketItemFacts
import com.ismartcoding.plain.enums.MediaType
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.features.system.RustMediaQuery
import java.io.File

abstract class BaseMediaContentHelper {
    protected abstract val uriExternal: Uri
    protected abstract val providerType: DataType
    protected abstract fun getProjection(): Array<String>
    protected abstract val mediaType: MediaType

    fun getItemUri(id: String): Uri {
        return Uri.withAppendedPath(uriExternal, id)
    }

    suspend fun countAsync(context: Context, query: String): Int = RustMediaQuery.count(providerType, query)

    suspend fun countAsync(context: Context, where: ContentWhere): Int = withIO {
        context.contentResolver.count(uriExternal, where)
    }

    suspend fun getPagingCursorAsync(context: Context, where: ContentWhere, limit: Int, offset: Int, sortBy: SortBy): Cursor? = withIO {
        context.contentResolver.getPagingCursor(
            uriExternal,
            getProjection(),
            where,
            limit,
            offset,
            sortBy,
        )
    }

    suspend fun getIdsAsync(context: Context, query: String): Set<String> = RustMediaQuery.ids(providerType, query)

    suspend fun getIdsAsync(context: Context, where: ContentWhere): Set<String> = withIO {
        context.contentResolver.getSearchCursor(
            uriExternal, arrayOf(BaseColumns._ID), where
        )?.map { cursor, cache ->
            cursor.getStringValue(BaseColumns._ID, cache)
        }?.toSet() ?: emptySet()
    }

    suspend fun getTrashedIdsAsync(context: Context, query: String): Set<String> = RustMediaQuery.ids(providerType, query, fromTrash = true)

    fun getPathsByIdsAsync(
        context: Context,
        ids: Set<String>,
    ): Set<String> {
        val paths = mutableSetOf<String>()
        val projection = arrayOf(BaseColumns._ID, MediaStore.MediaColumns.DATA)
        ids.chunked(500).forEach { chunk ->
            val where = ContentWhere()
            where.addIn(BaseColumns._ID, chunk)
            context.contentResolver.getSearchCursor(uriExternal, projection, where)?.forEach { cursor, cache ->
                val path = cursor.getStringValue(MediaStore.MediaColumns.DATA, cache)
                paths.add(path)
            }
        }

        return paths
    }

    fun getBucketItemFactsAsync(context: Context): List<MediaBucketItemFacts> = queryBucketItemFacts(
        context,
        if (mediaType == MediaType.AUDIO) {
            "${MediaStore.Audio.Media.DURATION} > 0 AND ${MediaStore.MediaColumns.BUCKET_DISPLAY_NAME} != ''"
        } else {
            "${MediaStore.MediaColumns.BUCKET_DISPLAY_NAME} != ''"
        },
        null,
    )

    protected fun queryBucketItemFacts(
        context: Context,
        selection: String,
        selectionArgs: Array<String>?,
    ): List<MediaBucketItemFacts> {
        val projection = arrayOf(
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATA,
        )
        return context.contentResolver.query(
            uriExternal,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.MediaColumns.DATE_MODIFIED} DESC",
        )?.map { cursor, cache ->
            val name = cursor.getStringValue(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME, cache)
            MediaBucketItemFacts(
                id = cursor.getStringValue(MediaStore.MediaColumns.BUCKET_ID, cache),
                name = name,
                size = cursor.getLongValue(MediaStore.MediaColumns.SIZE, cache),
                path = cursor.getStringValue(MediaStore.MediaColumns.DATA, cache),
            )
        }.orEmpty()
    }
}

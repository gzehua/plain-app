package com.ismartcoding.plain.audio


import com.ismartcoding.plain.graphql.models.Audio as AudioTrack
import com.ismartcoding.plain.helpers.ContentWhere
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.ismartcoding.plain.lib.extensions.getIntValue
import com.ismartcoding.plain.lib.extensions.getLongValue
import com.ismartcoding.plain.lib.extensions.getStringValue
import com.ismartcoding.plain.lib.extensions.getTimeSecondsValue
import com.ismartcoding.plain.lib.extensions.map
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.platform.isQPlus
import com.ismartcoding.plain.data.TagRelationStub
import com.ismartcoding.plain.enums.AppFeatureType
import com.ismartcoding.plain.enums.MediaType
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.enums.has
import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.features.file.toSortBy
import com.ismartcoding.plain.features.media.BaseMediaContentHelper

object AudioMediaStoreHelper : BaseMediaContentHelper() {
    override val providerType: DataType = DataType.AUDIO
    override val uriExternal: Uri = if (isQPlus()) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL) else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    override val mediaType: MediaType = MediaType.AUDIO

    override fun getProjection(): Array<String> {
        val projection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
        )

        if (isQPlus()) {
            projection.add(MediaStore.Audio.Media.BUCKET_ID)
            if (AppFeatureType.MEDIA_FAVORITE.has()) {
                projection.add(MediaStore.Audio.Media.IS_FAVORITE)
            }
        }

        return projection.toTypedArray()
    }

    suspend fun searchAsync(
        context: Context,
        query: String,
        limit: Int,
        offset: Int,
        sortBy: FileSortBy,
    ): List<AudioTrack> = com.ismartcoding.plain.features.system.RustMediaQuery.rows(com.ismartcoding.plain.enums.DataType.AUDIO, query, limit, offset, sortBy).map { it as AudioTrack }

    suspend fun searchAsync(
        context: Context,
        where: ContentWhere,
        limit: Int,
        offset: Int,
        sortBy: FileSortBy,
    ): List<AudioTrack> = withIO {
        val audios = mutableListOf<AudioTrack>()
        
        getPagingCursorAsync(context, where, limit, offset, sortBy.toSortBy())?.map { cursor, cache ->
            val id = cursor.getStringValue(MediaStore.Audio.Media._ID, cache)
            val title = cursor.getStringValue(MediaStore.Audio.Media.TITLE, cache)
            val artist = cursor.getStringValue(MediaStore.Audio.Media.ARTIST, cache).replace(MediaStore.UNKNOWN_STRING, "")
            val size = cursor.getLongValue(MediaStore.Audio.Media.SIZE, cache)
            val durationMs = cursor.getLongValue(MediaStore.Audio.Media.DURATION, cache)
            val createdAt = cursor.getTimeSecondsValue(MediaStore.Audio.Media.DATE_ADDED, cache)
            val updatedAt = cursor.getTimeSecondsValue(MediaStore.Audio.Media.DATE_MODIFIED, cache)
            val path = cursor.getStringValue(MediaStore.Audio.Media.DATA, cache)
            val bucketId = if (isQPlus()) {
                cursor.getStringValue(MediaStore.Audio.Media.BUCKET_ID, cache)
            } else ""
            val albumId = cursor.getStringValue(MediaStore.Audio.Media.ALBUM_ID, cache)
            val isFavorite = if (isQPlus()) cursor.getIntValue(MediaStore.Audio.Media.IS_FAVORITE, cache) == 1 else false

            val audio = AudioTrack(id = id, title = title, artist = artist, path = path, durationMs = durationMs, size = size, bucketId = bucketId, albumId = albumId, createdAt = createdAt, updatedAt = updatedAt, isFavorite = isFavorite)
            audios.add(audio)

        }
        
        
        audios
    }

    suspend fun getTagRelationStubsAsync(context: Context, query: String): List<TagRelationStub> =
        com.ismartcoding.plain.features.system.RustMediaQuery.tagStubs(com.ismartcoding.plain.enums.DataType.AUDIO, query)

}

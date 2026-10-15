package com.ismartcoding.plain.platform

import android.content.ContentUris
import android.net.Uri
import android.provider.DocumentsContract
import com.ismartcoding.plain.features.audio.AudioMedia
import android.provider.MediaStore
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.graphql.models.AudioItem
import com.ismartcoding.plain.lib.extensions.pathToUri
import com.ismartcoding.plain.lib.withIO

private val audioUri get() = if (isQPlus()) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL) else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
actual suspend fun audioLibraryMetadata(path: String): AudioItem = withIO {
    val retriever = android.media.MediaMetadataRetriever()
    try {
        retriever.setDataSource(appContext, path.pathToUri())
        AudioItem(
            title = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE).orEmpty().ifEmpty { java.io.File(path).nameWithoutExtension },
            path = path,
            artist = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST).orEmpty(),
            durationMs = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0,
        )
    } finally { retriever.release() }
}

actual suspend fun audioLibraryResolve(ids: List<String>, paths: List<String>): List<AudioMedia> = withIO {
    val systemIds = (ids + paths.mapNotNull { systemAudioMediaId(Uri.parse(it)) }).distinct()
    val filePaths = paths.mapNotNull { path -> val uri = Uri.parse(path); when (uri.scheme) { "content" -> null; "file" -> uri.path; else -> path } }.distinct()
    if (systemIds.isEmpty() && filePaths.isEmpty()) return@withIO emptyList()
    if (!Permission.WRITE_EXTERNAL_STORAGE.isGranted() && !Permission.READ_MEDIA_AUDIO.isGranted()) {
        return@withIO paths.mapNotNull { path ->
            val id = systemAudioMediaId(Uri.parse(path)) ?: return@mapNotNull null
            try { AudioMedia(audioLibraryMetadata(path), id).copy(aliases = listOf(path)) }
            catch (_: SecurityException) { null }
        }
    }
    val columns = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION)
    val results = mutableListOf<AudioMedia>()
    for ((column, values) in listOf(MediaStore.Audio.Media._ID to systemIds, MediaStore.Audio.Media.DATA to filePaths)) {
        for (batch in values.chunked(400)) {
            val selection = "$column IN (${batch.joinToString(",") { "?" }})"
            checkNotNull(appContext.contentResolver.query(audioUri, columns, selection, batch.toTypedArray(), null)) { "Audio catalog query failed" }.use { cursor ->
                while (cursor.moveToNext()) {
                    val mediaId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)).toString()
                    val filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)).orEmpty().ifEmpty { ContentUris.withAppendedId(audioUri, mediaId.toLong()).toString() }
                    results.add(AudioMedia(
                        mediaId = mediaId,
                        path = filePath,
                        title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)).orEmpty(),
                        artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)).orEmpty().replace(MediaStore.UNKNOWN_STRING, ""),
                        albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)).toString(),
                        durationMs = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                        aliases = paths.filter { path -> val uri = Uri.parse(path); if (uri.scheme == "content") systemAudioMediaId(uri) == mediaId else (if (uri.scheme == "file") uri.path else path) == filePath },
                    ))
                }
            }
        }
    }
    results.filter { it.path.isNotEmpty() }.distinctBy { it.mediaId }
}

private fun systemAudioMediaId(uri: Uri): String? {
    if (uri.scheme != "content") return null
    return when (uri.authority) {
        MediaStore.AUTHORITY -> runCatching { ContentUris.parseId(uri).toString() }.getOrNull()
        "com.android.providers.media.documents" -> runCatching {
            DocumentsContract.getDocumentId(uri).takeIf { it.startsWith("audio:") }?.substringAfter(':')?.toLongOrNull()?.toString()
        }.getOrNull()
        else -> null
    }
}

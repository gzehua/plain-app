package com.ismartcoding.plain.features.media.actions

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.app.*
import com.ismartcoding.plain.ui.app.*
import com.ismartcoding.plain.features.chat.*
import com.ismartcoding.plain.ui.chat.*
import com.ismartcoding.plain.features.feeds.*
import com.ismartcoding.plain.ui.feeds.*
import com.ismartcoding.plain.features.images.search.*
import com.ismartcoding.plain.features.sessions.*
import com.ismartcoding.plain.ui.sessions.*
import com.ismartcoding.plain.features.shares.*
import com.ismartcoding.plain.ui.shares.*
import com.ismartcoding.plain.platform.*
import com.ismartcoding.plain.platform.contacts.*
import com.ismartcoding.plain.platform.files.*
import com.ismartcoding.plain.ui.platform.files.*
import com.ismartcoding.plain.platform.media.*
import com.ismartcoding.plain.ui.platform.media.*
import com.ismartcoding.plain.platform.network.*
import com.ismartcoding.plain.platform.permissions.*
import com.ismartcoding.plain.app.*
import com.ismartcoding.plain.ui.app.*
import com.ismartcoding.plain.ui.navigation.*
import com.ismartcoding.plain.features.sessions.*
import com.ismartcoding.plain.ui.sessions.*
import com.ismartcoding.plain.features.settings.*
import com.ismartcoding.plain.ui.settings.*
import com.ismartcoding.plain.platform.files.*
import com.ismartcoding.plain.ui.platform.files.*
import com.ismartcoding.plain.platform.media.*
import com.ismartcoding.plain.ui.platform.media.*
import com.ismartcoding.plain.platform.notifications.*
import com.ismartcoding.plain.ui.platform.notifications.*
import com.ismartcoding.plain.app.*
import com.ismartcoding.plain.ui.app.*
import com.ismartcoding.plain.ui.navigation.*
import com.ismartcoding.plain.features.sessions.*
import com.ismartcoding.plain.ui.sessions.*
import com.ismartcoding.plain.features.settings.*
import com.ismartcoding.plain.ui.settings.*
import com.ismartcoding.plain.platform.files.*
import com.ismartcoding.plain.ui.platform.files.*
import com.ismartcoding.plain.platform.media.*
import com.ismartcoding.plain.ui.platform.media.*
import com.ismartcoding.plain.platform.notifications.*
import com.ismartcoding.plain.ui.platform.notifications.*
import com.ismartcoding.plain.platform.system.*
import com.ismartcoding.plain.ui.platform.system.*
import com.ismartcoding.plain.ui.state.*
import com.ismartcoding.plain.ui.state.*
import com.ismartcoding.plain.platform.telephony.*
import com.ismartcoding.plain.rust.http.*
import kotlinx.serialization.json.*
import java.io.File

internal object MediaProvider {
    fun handle(method: String, params: JsonObject): JsonElement {
        val type = DataType.entries.single { it.value == params.getValue("type").jsonPrimitive.int }
        require(type in setOf(DataType.AUDIO, DataType.VIDEO, DataType.IMAGE, DataType.DOC))
        val permission = when (type) {
            DataType.AUDIO -> Permission.READ_MEDIA_AUDIO
            DataType.VIDEO -> Permission.READ_MEDIA_VIDEOS
            DataType.IMAGE -> Permission.READ_MEDIA_IMAGES
            else -> Permission.WRITE_EXTERNAL_STORAGE
        }
        check(Permission.WRITE_EXTERNAL_STORAGE.isGranted() || permission.isGranted()) { "Media provider permission denied" }
        return when (method) {
            "mediaProviderRows" -> JsonArray(params.getValue("ids").jsonArray.mapNotNull { value ->
                val id = value.jsonPrimitive.content
                try { row(type, id, params.getValue("includeVolumes").jsonPrimitive.boolean) }
                catch (_: Exception) { null }
            })
            "mediaProviderWrite" -> JsonArray(params.getValue("writes").jsonArray.map { value ->
                val write = value.jsonObject
                val id = write.getValue("id").jsonPrimitive.content
                var count = 0
                var after: JsonObject? = null
                try {
                    val uri = Uri.parse(getMediaItemUriString(type, id))
                    val selection = "${MediaStore.MediaColumns.DATA} = ?"
                    val args = arrayOf(write.getValue("expectedPath").jsonPrimitive.content)
                    val extras = matching().apply {
                        putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, if (write.getValue("expectedTrash").jsonPrimitive.boolean) MediaStore.MATCH_ONLY else MediaStore.MATCH_EXCLUDE)
                        putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                        putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, args)
                    }
                    if (write.getValue("delete").jsonPrimitive.boolean) {
                        count = if (isRPlus()) appContext.contentResolver.delete(uri, extras) else appContext.contentResolver.delete(uri, selection, args)
                    } else {
                        val fields = ContentValues()
                        write["setTrash"]?.takeUnless { it is JsonNull }?.let {
                            check(isRPlus()) { "MediaProvider trash column unavailable" }
                            fields.put(MediaStore.MediaColumns.IS_TRASHED, if (it.jsonPrimitive.boolean) 1 else 0)
                        }
                        write["relativePath"]?.takeUnless { it is JsonNull }?.let { fields.put(MediaStore.MediaColumns.RELATIVE_PATH, it.jsonPrimitive.content) }
                        write["displayName"]?.takeUnless { it is JsonNull }?.let { fields.put(MediaStore.MediaColumns.DISPLAY_NAME, it.jsonPrimitive.content) }
                        count = if (isRPlus()) appContext.contentResolver.update(uri, fields, extras) else appContext.contentResolver.update(uri, fields, selection, args)
                    }
                    after = row(type, id, false)
                } catch (_: Exception) { count = 0 }
                buildJsonObject { put("id", id); put("count", count); put("row", after ?: JsonNull) }
            })
            else -> error("Unsupported media provider primitive")
        }
    }

    private fun matching() = Bundle().apply { putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_INCLUDE) }

    private fun row(type: DataType, id: String, volume: Boolean): JsonObject? {
        require(id.toLongOrNull() != null)
        val uri = Uri.parse(getMediaItemUriString(type, id))
        val columns = mutableListOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.MIME_TYPE)
        if (isRPlus()) columns.add(MediaStore.MediaColumns.IS_TRASHED)
        val cursor = if (isRPlus()) appContext.contentResolver.query(uri, columns.toTypedArray(), matching(), null)
            else appContext.contentResolver.query(uri, columns.toTypedArray(), null, null, null)
        return checkNotNull(cursor) { "Media provider query failed" }.use {
            if (!it.moveToFirst()) return@use null
            val path = checkNotNull(it.getString(it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)))
            val root = if (!volume) null else if (isRPlus()) appContext.getSystemService(StorageManager::class.java).getStorageVolume(File(path))?.directory
                else Environment.getExternalStorageDirectory()
            buildJsonObject {
                put("id", id)
                put("path", path)
                put("trashed", isRPlus() && it.getInt(it.getColumnIndexOrThrow(MediaStore.MediaColumns.IS_TRASHED)) != 0)
                put("mimeType", it.getString(it.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)) ?: "")
                put("volumeRoot", root?.canonicalPath?.let(::JsonPrimitive) ?: JsonNull)
            }
        }
    }
}

package com.ismartcoding.plain.features.system

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.db.IData
import com.ismartcoding.plain.audio.DAudio
import com.ismartcoding.plain.data.DImage
import com.ismartcoding.plain.data.DVideo
import com.ismartcoding.plain.data.DDoc
import kotlinx.serialization.json.*
import kotlin.time.Instant

internal object RustMediaQuery {
    private const val COMMON = "id title path size bucketId createdAt updatedAt"
    private fun root(type: DataType): String = when (type) {
        DataType.AUDIO -> "audios"
        DataType.IMAGE -> "images"
        DataType.VIDEO -> "videos"
        DataType.DOC -> "docs"
        else -> error("Unsupported media type: $type")
    }
    suspend fun rows(type: DataType, query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<IData> {
        val root = root(type)
        val extra = when (type) {
            DataType.AUDIO -> "artist durationMs albumId isFavorite"
            DataType.IMAGE -> "width height rotation takenAt isFavorite"
            DataType.VIDEO -> "durationMs width height rotation takenAt isFavorite"
            DataType.DOC -> "durationMs"
            else -> error("Unsupported media type: $type")
        }
        return PlainRust.mainQuery("$root(query: ${gql(query)}, limit: $limit, offset: $offset, sortBy: ${sortBy.name}) { $COMMON $extra }").getValue(root).jsonArray.map {
            val row = it.jsonObject
            val id = row.text("id")
            val title = row.text("title")
            val path = row.text("path")
            val size = row.getValue("size").jsonPrimitive.long
            val bucket = row.text("bucketId")
            val created = Instant.parse(row.text("createdAt"))
            val updated = Instant.parse(row.text("updatedAt"))
            when (type) {
                DataType.AUDIO -> DAudio(id, title, row.text("artist"), path, row.getValue("durationMs").jsonPrimitive.long,
                    size, bucket, row.text("albumId"), created, updated, row.getValue("isFavorite").jsonPrimitive.boolean)
                DataType.IMAGE -> DImage(id, title, path, size, row.int("width"), row.int("height"), row.int("rotation"),
                    bucket, created, updated, row.instantOrNull("takenAt"), row.getValue("isFavorite").jsonPrimitive.boolean)
                DataType.VIDEO -> DVideo(id, title, path, row.getValue("durationMs").jsonPrimitive.long, size,
                    row.int("width"), row.int("height"), row.int("rotation"), bucket, created, updated,
                    row.instantOrNull("takenAt"), row.getValue("isFavorite").jsonPrimitive.boolean)
                DataType.DOC -> DDoc(id, title, path, row.getValue("durationMs").jsonPrimitive.long, size, bucket, created, updated)
                else -> error("Unsupported media type: $type")
            }
        }
    }
    suspend fun count(type: DataType, query: String): Int {
        root(type)
        val field = type.name.lowercase() + "Count"
        return PlainRust.mainQuery("$field(query: ${gql(query)})").getValue(field).jsonPrimitive.int
    }
    suspend fun ids(type: DataType, query: String, fromTrash: Boolean = false): Set<String> {
        root(type)
        return PlainRust.mainQuery("mediaIds(type: ${type.name}, query: ${gql(query)}, fromTrash: $fromTrash)")
            .getValue("mediaIds").jsonArray.map { it.jsonPrimitive.content }.toSet()
    }
    suspend fun tagStubs(type: DataType, query: String): List<com.ismartcoding.plain.data.TagRelationStub> {
        val field = root(type)
        return PlainRust.mainQuery("$field(query: ${gql(query)}, limit: 2147483647, offset: 0, sortBy: DATE_DESC) { id title size }").getValue(field).jsonArray.map {
            val row = it.jsonObject
            com.ismartcoding.plain.data.TagRelationStub(row.text("id"), row.text("title"), row.getValue("size").jsonPrimitive.long)
        }
    }
    suspend fun docExtGroups(query: String): List<Pair<String, Int>> =
        PlainRust.mainQuery("docExtGroups(query: ${gql(query)}) { ext count }").getValue("docExtGroups").jsonArray.map {
            it.jsonObject.let { row -> row.text("ext") to row.int("count") }
        }
    private fun JsonObject.text(key: String): String = getValue(key).jsonPrimitive.content
    private fun JsonObject.int(key: String): Int = getValue(key).jsonPrimitive.int
    private fun JsonObject.instantOrNull(key: String): Instant? = getValue(key).let { if (it is JsonNull) null else Instant.parse(it.jsonPrimitive.content) }
}

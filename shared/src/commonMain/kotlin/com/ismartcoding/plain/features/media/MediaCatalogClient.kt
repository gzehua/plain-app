package com.ismartcoding.plain.features.media

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable

import com.ismartcoding.plain.models.Audio as AudioTrack
import com.ismartcoding.plain.models.Doc as Document
import com.ismartcoding.plain.models.Video as MediaVideo
import com.ismartcoding.plain.models.Image as MediaImage
import com.ismartcoding.plain.rust.graphql.gql
import com.ismartcoding.plain.rust.graphql.selectionQuery
import com.ismartcoding.plain.platform.media.getMediaItemUriString
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.IData
import kotlinx.serialization.json.*

object MediaCatalogClient {
    private const val COMMON = "id title path size bucketId createdAt updatedAt"
    private fun root(type: DataType): String = when (type) {
        DataType.AUDIO -> "audios"
        DataType.IMAGE -> "images"
        DataType.VIDEO -> "videos"
        DataType.DOC -> "docs"
        else -> error("Unsupported media type: $type")
    }
    internal fun fields(type: DataType): String {
        val extra = when (type) {
            DataType.AUDIO -> "artist durationMs albumId albumFileId isFavorite"
            DataType.IMAGE -> "width height rotation takenAt isFavorite"
            DataType.VIDEO -> "durationMs width height rotation takenAt isFavorite"
            DataType.DOC -> "durationMs extension"
            else -> error("Unsupported media type: $type")
        }
        return "$COMMON $extra"
    }
    suspend fun rows(type: DataType, query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<IData> {
        when (type) {
            DataType.CALL -> return com.ismartcoding.plain.features.telephony.CallsClient.search(query, limit, offset)
            DataType.CONTACT -> return com.ismartcoding.plain.features.contacts.ContactsClient.search(query, limit, offset)
            DataType.SMS -> return com.ismartcoding.plain.features.telephony.sms.RustSmsQuery.search(query, limit, offset)
            else -> Unit
        }
        val root = root(type)
        return RustGraphqlApi.query("$root(query: ${gql(query)}, limit: $limit, offset: $offset, sortBy: ${sortBy.name}) { ${fields(type)} }").getValue(root).jsonArray.map {
            val row = it.jsonObject
            when (type) {
                DataType.AUDIO -> com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement<AudioTrack>(row)
                DataType.IMAGE -> com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement<MediaImage>(row)
                DataType.VIDEO -> com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement<MediaVideo>(row)
                DataType.DOC -> com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement<Document>(row)
                else -> error("Unsupported media type: $type")
            }
        }
    }
    suspend fun shareUris(type: DataType, ids: Collection<String>): List<String> =
        readSelectedMediaPaths(root(type), ids) { RustGraphqlApi.query(it) }
            .map { (id, path) -> getMediaItemUriString(type, id, path) }

    suspend fun count(type: DataType, query: String): Int {
        when (type) {
            DataType.CALL -> return com.ismartcoding.plain.features.telephony.CallsClient.count(query)
            DataType.CONTACT -> return com.ismartcoding.plain.features.contacts.ContactsClient.count(query)
            DataType.SMS -> return com.ismartcoding.plain.features.telephony.sms.RustSmsQuery.count(query)
            else -> Unit
        }
        root(type)
        val field = type.name.lowercase() + "Count"
        return RustGraphqlApi.query("$field(query: ${gql(query)})").getValue(field).jsonPrimitive.int
    }
    suspend fun docExtGroups(query: String): List<Pair<String, Int>> =
        JsonHelper.jsonDecodeFromElement<List<DocExtensionGroup>>(RustGraphqlApi.query("docExtGroups(query: ${gql(query)}) { ext count }").getValue("docExtGroups")).map { it.ext to it.count }
    suspend fun buckets(dataType: DataType): List<com.ismartcoding.plain.models.MediaBucket> =
        com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement(RustGraphqlApi.query("mediaBuckets(type: ${dataType.name}) { id name itemCount size topItemPaths }").getValue("mediaBuckets"))

}

@Serializable
private data class DocExtensionGroup(val ext: String, val count: Int)

internal suspend fun readSelectedMediaPaths(
    root: String,
    ids: Collection<String>,
    query: suspend (String) -> JsonObject,
): List<Pair<String, String>> {
    if (ids.isEmpty()) return emptyList()
    val selected = ids.distinct()
    val rows = query("$root(query: ${gql(selectionQuery(selected))}, limit: ${selected.size}, offset: 0, sortBy: NAME_ASC) { id path }")
        .getValue(root).jsonArray
    val paths = rows.associate { it.jsonObject.getValue("id").jsonPrimitive.content to it.jsonObject.getValue("path").jsonPrimitive.content }
    return selected.map { id -> id to paths.getValue(id) }
}

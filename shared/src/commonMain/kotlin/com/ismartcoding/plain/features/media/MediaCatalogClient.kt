package com.ismartcoding.plain.features.media

import com.ismartcoding.plain.rust.graphql.GraphqlRequest
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable
import com.ismartcoding.plain.models.Audio as AudioTrack
import com.ismartcoding.plain.models.Doc as Document
import com.ismartcoding.plain.models.Video as MediaVideo
import com.ismartcoding.plain.models.Image as MediaImage
import com.ismartcoding.plain.rust.graphql.selectionQuery
import com.ismartcoding.plain.platform.media.getMediaItemUriString
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.IData
import kotlinx.serialization.json.*
import com.ismartcoding.plain.features.contacts.ContactsClient
import com.ismartcoding.plain.features.telephony.CallsClient
import com.ismartcoding.plain.features.telephony.sms.RustSmsQuery
import com.ismartcoding.plain.models.MediaBucket

object MediaCatalogClient {
    suspend fun rows(type: DataType, query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<IData> {
        when (type) {
            DataType.CALL -> return CallsClient.search(query, limit, offset)
            DataType.CONTACT -> return ContactsClient.search(query, limit, offset)
            DataType.SMS -> return RustSmsQuery.search(query, limit, offset)
            else -> Unit
        }
        val root = Query.mediaRoot(type)
        return RustGraphqlApi.graphql(Query.mediaRows(type, query, limit, offset, sortBy.name)).getValue(root).jsonArray.map {
            val row = it.jsonObject
            when (type) {
                DataType.AUDIO -> JsonHelper.jsonDecodeFromElement<AudioTrack>(row)
                DataType.IMAGE -> JsonHelper.jsonDecodeFromElement<MediaImage>(row)
                DataType.VIDEO -> JsonHelper.jsonDecodeFromElement<MediaVideo>(row)
                DataType.DOC -> JsonHelper.jsonDecodeFromElement<Document>(row)
                else -> error("Unsupported media type: $type")
            }
        }
    }
    suspend fun shareUris(type: DataType, ids: Collection<String>): List<String> =
        readSelectedMediaPaths(Query.mediaRoot(type), ids) { RustGraphqlApi.graphql(it) }
            .map { (id, path) -> getMediaItemUriString(type, id, path) }

    suspend fun count(type: DataType, query: String): Int {
        when (type) {
            DataType.CALL -> return CallsClient.count(query)
            DataType.CONTACT -> return ContactsClient.count(query)
            DataType.SMS -> return RustSmsQuery.count(query)
            else -> Unit
        }
        val field = Query.mediaCountField(type)
        return RustGraphqlApi.graphql(Query.mediaCount(type, query)).getValue(field).jsonPrimitive.int
    }
    suspend fun docExtGroups(query: String): List<Pair<String, Int>> =
        JsonHelper.jsonDecodeFromElement<List<DocExtensionGroup>>(RustGraphqlApi.graphql(Query.docExtGroups(query)).getValue("docExtGroups")).map { it.ext to it.count }
    suspend fun buckets(dataType: DataType): List<MediaBucket> =
        JsonHelper.jsonDecodeFromElement(RustGraphqlApi.graphql(Query.mediaBuckets(dataType.name)).getValue("mediaBuckets"))
}

@Serializable
private data class DocExtensionGroup(val ext: String, val count: Int)

internal suspend fun readSelectedMediaPaths(
    root: String,
    ids: Collection<String>,
    query: suspend (GraphqlRequest) -> JsonObject,
): List<Pair<String, String>> {
    if (ids.isEmpty()) return emptyList()
    val selected = ids.distinct()
    val rows = query(Query.selectedMediaPaths(root, selectionQuery(selected), selected.size))
        .getValue(root).jsonArray
    val paths = rows.associate { it.jsonObject.getValue("id").jsonPrimitive.content to it.jsonObject.getValue("path").jsonPrimitive.content }
    return selected.map { id -> id to paths.getValue(id) }
}

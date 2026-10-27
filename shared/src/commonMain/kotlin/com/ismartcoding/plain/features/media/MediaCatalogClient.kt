package com.ismartcoding.plain.features.media

import com.ismartcoding.plain.IData
import com.ismartcoding.plain.features.contacts.ContactsClient
import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.features.telephony.CallsClient
import com.ismartcoding.plain.features.telephony.sms.RustSmsQuery
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.Audio as AudioTrack
import com.ismartcoding.plain.models.Doc as Document
import com.ismartcoding.plain.models.Image as MediaImage
import com.ismartcoding.plain.models.MediaBucket
import com.ismartcoding.plain.models.Video as MediaVideo
import com.ismartcoding.plain.platform.media.getMediaItemUriString
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.rust.graphql.GraphqlClient
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.rust.graphql.selectionQuery
import kotlinx.serialization.Serializable

object MediaCatalogClient {
    suspend fun rows(type: DataType, query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<IData> {
        when (type) {
            DataType.CALL -> return CallsClient.search(query, limit, offset)
            DataType.CONTACT -> return ContactsClient.search(query, limit, offset)
            DataType.SMS -> return RustSmsQuery.search(query, limit, offset)
            else -> Unit
        }
        val request = Query.mediaRows(type, query, limit, offset, sortBy)
        return when (type) {
            DataType.AUDIO -> RustGraphqlApi.execute<List<AudioTrack>>(request)
            DataType.IMAGE -> RustGraphqlApi.execute<List<MediaImage>>(request)
            DataType.VIDEO -> RustGraphqlApi.execute<List<MediaVideo>>(request)
            DataType.DOC -> RustGraphqlApi.execute<List<Document>>(request)
            else -> error("Unsupported media type: $type")
        }
    }

    suspend fun shareUris(type: DataType, ids: Collection<String>): List<String> =
        readSelectedMediaPaths(Query.mediaRoot(type), ids)
            .map { (id, path) -> getMediaItemUriString(type, id, path) }

    suspend fun count(type: DataType, query: String): Int {
        when (type) {
            DataType.CALL -> return CallsClient.count(query)
            DataType.CONTACT -> return ContactsClient.count(query)
            DataType.SMS -> return RustSmsQuery.count(query)
            else -> Unit
        }
        return RustGraphqlApi.execute(Query.mediaCount(type, query))
    }

    suspend fun docExtGroups(query: String): List<Pair<String, Int>> =
        RustGraphqlApi.execute<List<DocExtensionGroup>>(Query.docExtGroups(query)).map { it.ext to it.count }

    suspend fun buckets(dataType: DataType): List<MediaBucket> =
        RustGraphqlApi.execute(Query.mediaBuckets(dataType))
}

@Serializable
private data class DocExtensionGroup(
    val ext: String,
    val count: Int,
)

internal suspend fun readSelectedMediaPaths(
    root: String,
    ids: Collection<String>,
    api: GraphqlClient = RustGraphqlApi,
): List<Pair<String, String>> {
    if (ids.isEmpty()) return emptyList()
    val selected = ids.distinct()
    val rows = api.execute<List<SelectedMediaPath>>(
        Query.selectedMediaPaths(root, selectionQuery(selected), selected.size),
    )
    val paths = rows.associate { it.id to it.path }
    return selected.map { id -> id to paths.getValue(id) }
}

@Serializable
private data class SelectedMediaPath(
    val id: String,
    val path: String,
)

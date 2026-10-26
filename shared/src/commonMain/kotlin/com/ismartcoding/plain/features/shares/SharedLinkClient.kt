package com.ismartcoding.plain.features.shares

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.features.chat.models.MessageShare
import kotlinx.serialization.json.*

object SharedLinkClient {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun pageUrl(link: SharedLink): String = link.pageUrl

    suspend fun ownLink(id: String, host: String? = null): SharedLink =
        json.decodeFromJsonElement(RustGraphqlApi.graphql(Query.shareLink(id, host)).getValue("shareLink"))

    suspend fun initialLink(messageId: String, expected: MessageShare): SharedLink =
        json.decodeFromJsonElement(RustGraphqlApi.graphql(
            Query.initialSharedLink(messageId, expected),
        ).getValue("initialSharedLink"))

    suspend fun browse(messageId: String, expected: MessageShare, virtualPath: String?): SharedBrowseResult? =
        RustGraphqlApi.graphql(
            Mutation.browseSharedLink(messageId, expected, virtualPath),
        ).getValue("browseSharedLink").takeUnless { it is JsonNull }?.let { json.decodeFromJsonElement(it) }

    suspend fun cacheFile(link: SharedLink, urlToken: String, entry: SharedFileDto, thumbnail: Boolean = false): String =
        RustGraphqlApi.graphql(
            Mutation.cacheSharedFile(link, urlToken, entry, thumbnail),
        ).getValue("cacheSharedFile").jsonPrimitive.content
}

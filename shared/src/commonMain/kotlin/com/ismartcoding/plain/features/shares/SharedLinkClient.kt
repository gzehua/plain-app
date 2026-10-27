package com.ismartcoding.plain.features.shares

import com.ismartcoding.plain.features.chat.models.MessageShare
import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

object SharedLinkClient {
    fun pageUrl(link: SharedLink): String = link.pageUrl

    suspend fun ownLink(id: String, host: String? = null): SharedLink =
        RustGraphqlApi.execute(Query.shareLink(id, host))

    suspend fun initialLink(messageId: String, expected: MessageShare): SharedLink =
        RustGraphqlApi.execute(Query.initialSharedLink(messageId, expected))

    suspend fun browse(messageId: String, expected: MessageShare, virtualPath: String?): SharedBrowseResult? =
        RustGraphqlApi.execute(Mutation.browseSharedLink(messageId, expected, virtualPath))

    suspend fun cacheFile(
        link: SharedLink,
        urlToken: String,
        entry: SharedFileDto,
        thumbnail: Boolean = false,
    ): String =
        RustGraphqlApi.execute(Mutation.cacheSharedFile(link, urlToken, entry, thumbnail))
}

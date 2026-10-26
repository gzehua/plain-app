package com.ismartcoding.plain.features.shares

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.ShareRecord
import com.ismartcoding.plain.lib.JsonHelper
import kotlin.time.Instant
import kotlinx.serialization.json.*

object SharesClient {
    suspend fun createShare(name: String, realPaths: List<String>, readOnly: Boolean, expiresAt: Instant?): ShareRecord =
        RustGraphqlApi.graphql(Mutation.createShare(name, realPaths, readOnly, expiresAt))
            .getValue("createShare").share()

    suspend fun updateShare(id: String, name: String, expiresAt: Instant?, realPaths: List<String>? = null): ShareRecord =
        RustGraphqlApi.graphql(Mutation.updateShare(id, name, expiresAt, realPaths))
            .getValue("updateShare").share()

    suspend fun deleteShare(id: String) { RustGraphqlApi.graphql(Mutation.deleteShare(id)) }
    suspend fun listShares(): List<ShareRecord> = RustGraphqlApi.graphql(Query.shareRecords()).getValue("shareRecords").jsonArray.map { it.share() }
    suspend fun getShare(id: String): ShareRecord? = RustGraphqlApi.graphql(Query.shareRecord(id))["shareRecord"]?.takeUnless { it is JsonNull }?.share()

    suspend fun buildLink(share: ShareRecord, host: String? = null): String = SharedLinkClient.pageUrl(SharedLinkClient.ownLink(share.id, host))

    private fun JsonElement.share(): ShareRecord = JsonHelper.jsonDecodeFromElement<ShareRecord>(this)
}

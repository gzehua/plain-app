package com.ismartcoding.plain.features.shares

import com.ismartcoding.plain.models.ShareRecord
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.request.RustApi
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.rust.request.gqlIds
import kotlin.time.Instant
import kotlinx.serialization.json.*

object SharesClient {
    private const val FIELDS = "id name password urlToken readOnly expiresAt createdAt updatedAt roots { virtualPath realPath isDir }"

    suspend fun createShare(name: String, realPaths: List<String>, readOnly: Boolean, expiresAt: Instant?): ShareRecord =
        RustApi.mutate("createShare(name: ${gql(name)}, realPaths: ${gqlIds(realPaths)}, readOnly: $readOnly, expiresAt: ${expiry(expiresAt)}) { $FIELDS }")
            .getValue("createShare").share()

    suspend fun updateShare(id: String, name: String, expiresAt: Instant?, realPaths: List<String>? = null): ShareRecord =
        RustApi.mutate("updateShare(id: ${gql(id)}, name: ${gql(name)}, expiresAt: ${expiry(expiresAt)}, realPaths: ${realPaths?.let(::gqlIds) ?: "null"}) { $FIELDS }")
            .getValue("updateShare").share()

    suspend fun deleteShare(id: String) { RustApi.mutate("deleteShare(id: ${gql(id)})") }
    suspend fun listShares(): List<ShareRecord> = RustApi.query("shareRecords { $FIELDS }").getValue("shareRecords").jsonArray.map { it.share() }
    suspend fun getShare(id: String): ShareRecord? = RustApi.query("shareRecord(id: ${gql(id)}) { $FIELDS }")["shareRecord"]?.takeUnless { it is JsonNull }?.share()

    suspend fun buildLink(share: ShareRecord, host: String? = null): String = SharedLinkClient.pageUrl(SharedLinkClient.ownLink(share.id, host))

    private fun expiry(value: Instant?): String = value?.let { gql(it.toString()) } ?: "null"
    private fun JsonElement.share(): ShareRecord = JsonHelper.jsonDecodeFromElement<ShareRecord>(this)
}

package com.ismartcoding.plain.features.shares

import com.ismartcoding.plain.api.graphql.models.Root
import com.ismartcoding.plain.api.graphql.models.ShareRecord
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.features.feeds.*
import com.ismartcoding.plain.ui.feeds.*
import com.ismartcoding.plain.features.files.*
import com.ismartcoding.plain.ui.files.*
import com.ismartcoding.plain.features.notes.*
import com.ismartcoding.plain.ui.notes.*
import com.ismartcoding.plain.features.tags.*
import com.ismartcoding.plain.ui.tags.*
import com.ismartcoding.plain.rust.request.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.request.*
import kotlin.io.encoding.Base64
import kotlin.time.Instant
import kotlinx.serialization.json.*

object SharesClient {
    private const val FIELDS = "id name password urlToken readOnly expiresAt createdAt updatedAt roots { virtualPath realPath isDir }"

    suspend fun createShare(name: String, realPaths: List<String>, readOnly: Boolean, expiresAt: Instant?): ShareRecord =
        RustContentApi.mutate("createShare(name: ${gql(name)}, realPaths: ${gqlIds(realPaths)}, readOnly: $readOnly, expiresAt: ${expiry(expiresAt)}) { $FIELDS }")
            .getValue("createShare").share()

    suspend fun updateShare(id: String, name: String, expiresAt: Instant?, realPaths: List<String>? = null): ShareRecord =
        RustContentApi.mutate("updateShare(id: ${gql(id)}, name: ${gql(name)}, expiresAt: ${expiry(expiresAt)}, realPaths: ${realPaths?.let(::gqlIds) ?: "null"}) { $FIELDS }")
            .getValue("updateShare").share()

    suspend fun deleteShare(id: String) { RustContentApi.mutate("deleteShare(id: ${gql(id)})") }
    suspend fun listShares(): List<ShareRecord> = RustContentApi.query("shareRecords { $FIELDS }").getValue("shareRecords").jsonArray.map { it.share() }
    suspend fun getShare(id: String): ShareRecord? = RustContentApi.query("shareRecord(id: ${gql(id)}) { $FIELDS }")["shareRecord"]?.takeUnless { it is JsonNull }?.share()
    suspend fun sharedToken(id: String): String = RustContentApi.query("shareToken(id: ${gql(id)})").string("shareToken")

    suspend fun buildLink(share: ShareRecord, host: String? = null): String = SharedLinkClient.pageUrl(SharedLinkClient.ownLink(share.id, host))

    suspend fun loadAuth(id: String): SharedAuth? {
        val auth = RustContentApi.query("shareAuth(id: ${gql(id)}) { token share { $FIELDS } }")["shareAuth"]?.takeUnless { it is JsonNull }?.jsonObject ?: return null
        return SharedAuth(auth.getValue("share").share(), Base64.UrlSafe.decode(auth.string("token")))
    }
    suspend fun resolveVirtualPath(share: ShareRecord, virtualPath: String): String? =
        RustContentApi.query("resolveSharePath(id: ${gql(share.id)}, virtualPath: ${gql(virtualPath)})")["resolveSharePath"]?.takeUnless { it is JsonNull }?.jsonPrimitive?.content

    suspend fun resolveSharedPath(sid: String, id: String): String? =
        RustContentApi.query("resolveShareFile(id: ${gql(sid)}, fileId: ${gql(id)})")["resolveShareFile"]?.takeUnless { it is JsonNull }?.jsonPrimitive?.content

    suspend fun browse(id: String, virtualPath: String): Pair<ShareRecord, List<Root>> {
        val row = RustContentApi.query("browseShare(id: ${gql(id)}, virtualPath: ${gql(virtualPath)}) { share { $FIELDS } entries { virtualPath realPath isDir } }").getValue("browseShare").jsonObject
        return row.getValue("share").share() to row.getValue("entries").jsonArray.map { it.root() }
    }
    private fun JsonElement.root(): Root = JsonHelper.jsonDecodeFromElement<Root>(this)
    private fun expiry(value: Instant?): String = value?.let { gql(it.toString()) } ?: "null"
    private fun JsonElement.share(): ShareRecord = JsonHelper.jsonDecodeFromElement<ShareRecord>(this)
}

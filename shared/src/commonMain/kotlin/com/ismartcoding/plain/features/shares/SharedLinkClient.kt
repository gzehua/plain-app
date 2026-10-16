package com.ismartcoding.plain.features.shares

import com.ismartcoding.plain.features.chat.models.MessageShare
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import kotlinx.serialization.json.*

object SharedLinkClient {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun pageUrl(link: SharedLink): String = link.pageUrl

    private const val LINK_FIELDS = "host port sharedId token pageUrl"
    private const val ENTRY_FIELDS = "name virtualPath isDir size mimeType hasThumb"
    private const val INFO_FIELDS = "name readOnly requiresPassword expiresAt urlToken entries { $ENTRY_FIELDS }"
    private const val CARD_FIELDS = "shareId urlToken peerInfo { id ip port } name itemCount totalSize expiresAt"

    suspend fun ownLink(id: String, host: String? = null): SharedLink =
        json.decodeFromJsonElement(PlainRust.mainQuery("shareLink(id: ${gql(id)}, host: ${host?.let(::gql) ?: "null"}) { $LINK_FIELDS }").getValue("shareLink"))

    suspend fun initialLink(messageId: String, expected: MessageShare): SharedLink =
        json.decodeFromJsonElement(PlainRust.mainGraphql(
            "query(\$messageId: ID!, \$expected: SharedMessageCardInput!) { initialSharedLink(messageId: \$messageId, expected: \$expected) { $LINK_FIELDS } }",
            mapOf("messageId" to JsonPrimitive(messageId), "expected" to json.encodeToJsonElement(expected)),
        ).getValue("initialSharedLink"))

    suspend fun browse(messageId: String, expected: MessageShare, virtualPath: String?): SharedBrowseResult? =
        PlainRust.mainGraphql(
            "mutation(\$messageId: ID!, \$expected: SharedMessageCardInput!, \$virtualPath: String) { browseSharedLink(messageId: \$messageId, expected: \$expected, virtualPath: \$virtualPath) { info { $INFO_FIELDS } link { $LINK_FIELDS } card { $CARD_FIELDS } } }",
            mapOf("messageId" to JsonPrimitive(messageId), "expected" to json.encodeToJsonElement(expected), "virtualPath" to (virtualPath?.let(::JsonPrimitive) ?: JsonNull)),
            longRunning = true,
        ).getValue("browseSharedLink").takeUnless { it is JsonNull }?.let { json.decodeFromJsonElement(it) }

    suspend fun fetchSharedInfo(link: SharedLink, virtualPath: String?): SharedInfoDto =
        json.decodeFromJsonElement(PlainRust.mainGraphql(
            "query(\$link: SharedLinkInput!, \$virtualPath: String) { sharedLinkInfo(link: \$link, virtualPath: \$virtualPath) { $INFO_FIELDS } }",
            mapOf("link" to json.encodeToJsonElement(link), "virtualPath" to (virtualPath?.let(::JsonPrimitive) ?: JsonNull)),
            longRunning = true,
        ).getValue("sharedLinkInfo"))

    suspend fun fileUrl(link: SharedLink, urlToken: String, virtualPath: String): String =
        PlainRust.mainGraphql(
            "query(\$link: SharedLinkInput!, \$urlToken: String!, \$virtualPath: String!) { sharedFileUrl(link: \$link, urlToken: \$urlToken, virtualPath: \$virtualPath) }",
            mapOf("link" to json.encodeToJsonElement(link), "urlToken" to JsonPrimitive(urlToken), "virtualPath" to JsonPrimitive(virtualPath)),
        ).getValue("sharedFileUrl").jsonPrimitive.content

    suspend fun cacheFile(link: SharedLink, urlToken: String, entry: SharedFileDto, thumbnail: Boolean = false): String =
        PlainRust.mainGraphql(
            "mutation(\$link: SharedLinkInput!, \$urlToken: String!, \$entry: SharedEntryInput!, \$thumbnail: Boolean!) { cacheSharedFile(link: \$link, urlToken: \$urlToken, entry: \$entry, thumbnail: \$thumbnail) }",
            mapOf("link" to json.encodeToJsonElement(link), "urlToken" to JsonPrimitive(urlToken), "entry" to json.encodeToJsonElement(entry), "thumbnail" to JsonPrimitive(thumbnail)),
            longRunning = true,
        ).getValue("cacheSharedFile").jsonPrimitive.content
}

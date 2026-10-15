package com.ismartcoding.plain.features.share

import com.ismartcoding.plain.chat.models.MessageShare
import com.ismartcoding.plain.lib.TimeHelper
import com.ismartcoding.plain.rust.request.RustContentApi
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
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

    suspend fun plan(kind: ShareBatchType, link: SharedLink, entries: List<SharedFileDto>, targetDir: String, downloadsBase: String): SharedBatchPlan =
        json.decodeFromJsonElement(PlainRust.mainGraphql(
            "mutation(\$kind: SharedDownloadKind!, \$link: SharedLinkInput!, \$entries: [SharedEntryInput!]!, \$targetDir: String!, \$downloadsBase: String!) { planSharedDownload(kind: \$kind, link: \$link, entries: \$entries, targetDir: \$targetDir, downloadsBase: \$downloadsBase) { targets { entry { $ENTRY_FIELDS } writeDir storeToDownloads entryName } totalFiles totalSize } }",
            mapOf("kind" to JsonPrimitive(kind.name), "link" to json.encodeToJsonElement(link), "entries" to json.encodeToJsonElement(entries), "targetDir" to JsonPrimitive(targetDir), "downloadsBase" to JsonPrimitive(downloadsBase)),
            longRunning = true,
        ).getValue("planSharedDownload"))

    suspend fun fileUrl(link: SharedLink, urlToken: String, virtualPath: String): String =
        PlainRust.mainGraphql(
            "query(\$link: SharedLinkInput!, \$urlToken: String!, \$virtualPath: String!) { sharedFileUrl(link: \$link, urlToken: \$urlToken, virtualPath: \$virtualPath) }",
            mapOf("link" to json.encodeToJsonElement(link), "urlToken" to JsonPrimitive(urlToken), "virtualPath" to JsonPrimitive(virtualPath)),
        ).getValue("sharedFileUrl").jsonPrimitive.content

    suspend fun downloadTo(
        url: String,
        write: (buffer: ByteArray, length: Int) -> Unit,
        onProgress: suspend (downloaded: Long, total: Long) -> Unit = { _, _ -> },
    ) {
        RustContentApi.postStream("shares/client/file", buildJsonObject { put("url", url) }).use { response ->
            check(response.isSuccess()) { "Shared download HTTP ${response.status.value}" }
            val total = response.header("Content-Length")?.toLongOrNull() ?: -1L
            val buffer = ByteArray(64 * 1024)
            var downloaded = 0L
            var lastReport = 0L
            while (true) {
                currentCoroutineContext().ensureActive()
                val read = response.channel.readAvailable(buffer)
                if (read == -1) break
                if (read > 0) {
                    write(buffer, read)
                    downloaded += read
                }
                val now = TimeHelper.nowMillis()
                if (now - lastReport > 300) { onProgress(downloaded, total); lastReport = now }
            }
            currentCoroutineContext().ensureActive()
            onProgress(downloaded, total)
        }
    }

}

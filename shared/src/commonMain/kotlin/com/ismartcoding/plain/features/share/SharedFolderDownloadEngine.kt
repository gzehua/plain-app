package com.ismartcoding.plain.features.share

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.features.download.*
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.getDownloadsDirPath
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*

object SharedFolderDownloadEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val intents = Channel<JsonObject>(128)
    private val stateLock = Mutex()
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val version = DownloadQueueVersion()

    init {
        scope.launch {
            for (body in intents) {
                try { call(body); refresh() }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { LogCat.e("Shared download request", error) }
            }
        }
    }
    fun enqueueFile(messageId: String, link: SharedLink, urlToken: String, entry: SharedFileDto, targetDir: String) =
        enqueue(messageId, ShareBatchType.FILE, link, urlToken, listOf(entry), targetDir)
    fun enqueueDirSync(messageId: String, link: SharedLink, urlToken: String, entry: SharedFileDto, targetDir: String) =
        enqueue(messageId, ShareBatchType.SYNC, link, urlToken, listOf(entry), targetDir)
    fun enqueueMulti(messageId: String, link: SharedLink, urlToken: String, entries: List<SharedFileDto>, targetDir: String) =
        enqueue(messageId, ShareBatchType.MULTI, link, urlToken, entries, targetDir)
    fun enqueueZip(messageId: String, link: SharedLink, urlToken: String, entries: List<SharedFileDto>, zipName: String) =
        enqueue(messageId, ShareBatchType.ZIP, link, urlToken, entries, "", zipName)
    private fun enqueue(messageId: String, type: ShareBatchType, link: SharedLink, urlToken: String, entries: List<SharedFileDto>, targetDir: String, zipName: String = "") =
        submit(buildJsonObject {
            put("action", "enqueue")
            put("intent", buildJsonObject {
                put("messageId", messageId); put("kind", type.name); put("link", json.encodeToJsonElement(link))
                put("urlToken", urlToken); put("entries", json.encodeToJsonElement(entries)); put("targetDir", targetDir)
                put("downloadsBase", "${getDownloadsDirPath().trimEnd('/')}/PlainApp"); put("zipName", zipName)
            })
        })
    fun retryFailed(taskId: String) { control(taskId, "retry") }
    fun control(id: String, command: String): Boolean = submit(buildJsonObject {
        put("action", "control"); put("id", id); put("command", command)
    })
    private fun submit(body: JsonObject): Boolean = intents.trySend(body).isSuccess.also {
        if (!it) LogCat.e("Shared download request capacity exceeded")
    }
    suspend fun refresh() = stateLock.withLock {
        val snapshot = call(buildJsonObject { put("action", "snapshot") }).jsonObject
        val current = snapshot.getValue("revision").jsonPrimitive.long
        if (!version.accept(snapshot.getValue("runtimeId").jsonPrimitive.content, current)) return@withLock
        val tasks = snapshot.getValue("tasks").jsonArray.map { value ->
            val row = value.jsonObject
            fun str(key: String) = row.getValue(key).jsonPrimitive.content
            fun number(key: String) = row.getValue(key).jsonPrimitive.long
            SharedFolderBatchTask(
                str("id"), str("messageId"), ShareBatchType.valueOf(str("type")), str("title"), str("targetDir"),
                json.decodeFromJsonElement(row.getValue("link")), str("urlToken"), json.decodeFromJsonElement(row.getValue("entries")), str("zipName"),
            ).apply {
                status = DownloadStatus.valueOf(str("status")); error = str("error")
                downloadedSize = number("downloadedSize"); totalSize = number("totalSize"); downloadSpeed = number("downloadSpeed")
                totalFiles = number("totalFiles").toInt(); doneFiles = number("doneFiles").toInt(); failedFiles = number("failedFiles").toInt()
                currentFile = str("currentFile"); packing = row.getValue("packing").jsonPrimitive.boolean
                failures = row.getValue("failures").jsonArray.map { item ->
                    DownloadFailure(item.jsonObject.getValue("path").jsonPrimitive.content, item.jsonObject.getValue("error").jsonPrimitive.content)
                }.toMutableList()
            }
        }
        DownloadCenter.replaceExternal(DOWNLOAD_KIND_SHARE, tasks)
    }
    private const val QUEUE_FIELDS = "runtimeId revision tasks { id messageId type title targetDir link { host port sharedId token pageUrl } urlToken entries { name virtualPath isDir size mimeType hasThumb } zipName generation status error downloadedSize totalSize downloadSpeed totalFiles doneFiles failedFiles currentFile packing failures { path error } }"
    private suspend fun call(body: JsonObject): JsonElement {
        return when (val action = body.getValue("action").jsonPrimitive.content) {
            "snapshot" -> PlainRust.mainQuery("sharedDownloadQueue { $QUEUE_FIELDS }").getValue("sharedDownloadQueue")
            "enqueue" -> PlainRust.mainGraphql("mutation(\$input: SharedDownloadInput!) { enqueueSharedDownload(input: \$input) }", mapOf("input" to body.getValue("intent")), longRunning = true).getValue("enqueueSharedDownload")
            "control" -> {
                val operation = when (body.getValue("command").jsonPrimitive.content) {
                    "pause" -> "pauseSharedDownload"
                    "resume" -> "resumeSharedDownload"
                    "retry" -> "retrySharedDownload"
                    "cancel" -> "cancelSharedDownload"
                    "remove" -> "removeSharedDownload"
                    else -> error("Unknown shared download control")
                }
                PlainRust.mainMutate("$operation(id: ${gql(body.getValue("id").jsonPrimitive.content)})", longRunning = true).getValue(operation)
            }
            else -> error("Unknown shared download operation: $action")
        }
    }
}

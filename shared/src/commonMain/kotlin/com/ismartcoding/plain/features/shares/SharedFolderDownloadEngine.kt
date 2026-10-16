package com.ismartcoding.plain.features.shares

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.features.downloads.*
import com.ismartcoding.plain.lib.logcat.LogCat
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

object SharedFolderDownloadEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val intents = Channel<Request>(128)
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
        submit(Request.Enqueue(SharedDownloadInput(messageId, type, link, urlToken, entries, targetDir, zipName)))
    fun retryFailed(taskId: String) { control(taskId, "retry") }
    fun control(id: String, command: String): Boolean = submit(Request.Control(id, command))
    private fun submit(request: Request): Boolean = intents.trySend(request).isSuccess.also {
        if (!it) LogCat.e("Shared download request capacity exceeded")
    }
    suspend fun refresh() = stateLock.withLock {
        val snapshot = call(Request.Snapshot).jsonObject
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
    private suspend fun call(request: Request): JsonElement = when (request) {
        Request.Snapshot -> PlainRust.mainQuery("sharedDownloadQueue { $QUEUE_FIELDS }").getValue("sharedDownloadQueue")
        is Request.Enqueue -> PlainRust.mainGraphql("mutation(\$input: SharedDownloadInput!) { enqueueSharedDownload(input: \$input) }", mapOf("input" to json.encodeToJsonElement(request.input)), longRunning = true).getValue("enqueueSharedDownload")
        is Request.Control -> {
            val operation = when (request.command) {
                "pause" -> "pauseSharedDownload"
                "resume" -> "resumeSharedDownload"
                "retry" -> "retrySharedDownload"
                "cancel" -> "cancelSharedDownload"
                "remove" -> "removeSharedDownload"
                else -> error("Unknown shared download control")
            }
            PlainRust.mainMutate("$operation(id: ${gql(request.id)})", longRunning = true).getValue(operation)
        }
    }

    private sealed interface Request {
        data object Snapshot : Request
        data class Enqueue(val input: SharedDownloadInput) : Request
        data class Control(val id: String, val command: String) : Request
    }
}

@Serializable
internal data class SharedDownloadInput(
    val messageId: String,
    val kind: ShareBatchType,
    val link: SharedLink,
    val urlToken: String,
    val entries: List<SharedFileDto>,
    val targetDir: String,
    val zipName: String,
)

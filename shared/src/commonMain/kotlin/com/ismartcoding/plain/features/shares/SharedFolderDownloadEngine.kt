package com.ismartcoding.plain.features.shares

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.lib.JsonHelper
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
        val snapshot = JsonHelper.jsonDecodeFromElement<SharedDownloadSnapshot>(call(Request.Snapshot))
        if (!version.accept(snapshot.runtimeId, snapshot.revision)) return@withLock
        val tasks = snapshot.tasks.map { row ->
            SharedFolderBatchTask(
                row.id, row.messageId, ShareBatchType.valueOf(row.type), row.title, row.targetDir,
                row.link, row.urlToken, row.entries, row.zipName,
            ).apply {
                status = DownloadStatus.valueOf(row.status)
                error = row.error
                downloadedSize = row.downloadedSize
                totalSize = row.totalSize
                downloadSpeed = row.downloadSpeed
                totalFiles = row.totalFiles.toInt()
                doneFiles = row.doneFiles.toInt()
                failedFiles = row.failedFiles.toInt()
                currentFile = row.currentFile
                packing = row.packing
                failures = row.failures.map { DownloadFailure(it.path, it.error) }.toMutableList()
            }
        }
        DownloadCenter.replaceExternal(DOWNLOAD_KIND_SHARE, tasks)
    }

    private suspend fun call(request: Request): JsonElement = when (request) {
        Request.Snapshot -> RustGraphqlApi.graphql(Query.sharedDownloadQueue()).getValue("sharedDownloadQueue")
        is Request.Enqueue -> RustGraphqlApi.graphql(Mutation.enqueueSharedDownload(request.input)).getValue("enqueueSharedDownload")
        is Request.Control -> {
            val operation = Mutation.sharedDownloadControl(request.command, request.id)
            RustGraphqlApi.graphql(operation).getValue(operation.field)
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

@Serializable
private data class SharedDownloadSnapshot(val runtimeId: String, val revision: Long, val tasks: List<SharedDownloadRow>)

@Serializable
private data class SharedDownloadRow(
    val id: String,
    val messageId: String,
    val type: String,
    val title: String,
    val targetDir: String,
    val link: SharedLink,
    val urlToken: String,
    val entries: List<SharedFileDto>,
    val zipName: String,
    val status: String,
    val error: String,
    val downloadedSize: Long,
    val totalSize: Long,
    val downloadSpeed: Long,
    val totalFiles: Long,
    val doneFiles: Long,
    val failedFiles: Long,
    val currentFile: String,
    val packing: Boolean,
    val failures: List<SharedDownloadFailure>,
)

@Serializable
private data class SharedDownloadFailure(val path: String, val error: String)

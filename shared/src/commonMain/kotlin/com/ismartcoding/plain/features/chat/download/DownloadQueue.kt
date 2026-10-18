package com.ismartcoding.plain.features.chat.download

import com.ismartcoding.plain.features.chat.models.MessageFile
import com.ismartcoding.plain.api.graphql.models.Peer
import com.ismartcoding.plain.features.chat.peer.RustPeerStore
import com.ismartcoding.plain.features.downloads.DOWNLOAD_KIND_CHAT
import com.ismartcoding.plain.features.downloads.DownloadCenter
import com.ismartcoding.plain.features.downloads.DownloadQueueVersion
import com.ismartcoding.plain.features.downloads.DownloadStatus
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*
import kotlinx.serialization.Serializable

object DownloadQueue {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val intents = Channel<Request>(128)
    private val stateLock = Mutex()
    private val version = DownloadQueueVersion()
    val downloadProgress = MutableStateFlow<Map<String, DownloadTask>>(emptyMap())

    init {
        scope.launch {
            for (body in intents) {
                try {
                    call(body)
                    refresh()
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { LogCat.e("Rust download request", error) }
            }
        }
    }
    fun addDownloadTask(messageFile: MessageFile, peer: Peer, messageId: String): String {
        submit(Request.Enqueue(messageId, messageFile.id, peer.id))
        return messageFile.id
    }
    fun pauseDownload(taskId: String): Boolean = control(taskId, "pause")
    fun resumeDownload(taskId: String): Boolean = control(taskId, "resume")
    fun retryDownload(taskId: String): Boolean = control(taskId, "retry")
    fun removeDownload(taskId: String): Boolean = control(taskId, "remove")
    private fun control(id: String, command: String) = submit(Request.Control(id, command))
    private fun submit(body: Request): Boolean = intents.trySend(body).isSuccess.also { accepted ->
        if (!accepted) LogCat.e("Rust download request capacity exceeded")
    }

    suspend fun refresh() = stateLock.withLock {
        val snapshot = JsonHelper.jsonDecodeFromElement<ChatDownloadSnapshot>(call(Request.Snapshot))
        if (!version.accept(snapshot.runtimeId, snapshot.revision)) return@withLock
        val tasks = snapshot.tasks.map { row ->
            DownloadTask(
                messageFile = row.file,
                peer = row.peer,
                messageId = row.messageId,
                status = DownloadStatus.valueOf(row.status),
                error = row.error,
                downloadedSize = row.downloaded,
                downloadSpeed = row.speed,
            )
        }.associateBy { it.id }
        downloadProgress.value = tasks
        DownloadCenter.replaceExternal(DOWNLOAD_KIND_CHAT, tasks.values.toList())
    }
    private const val QUEUE_FIELDS = "runtimeId revision tasks { id messageId status error downloaded total speed file { id: fileId uri size durationMs width height summary fileName } peer { id name ip key publicKey status port deviceType token createdAt updatedAt } }"
    private suspend fun call(request: Request): JsonElement {
        if (request == Request.Snapshot) return PlainRust.mainQuery("chatDownloadQueue { $QUEUE_FIELDS }").getValue("chatDownloadQueue")
        val operation = when (request) {
            is Request.Enqueue -> "enqueueChatDownload(messageId: ${gql(request.messageId)}, fileId: ${gql(request.fileId)}, peerId: ${gql(request.peerId)})"
            is Request.Control -> {
                val field = when (request.command) {
                    "pause" -> "pauseChatDownload"
                    "resume" -> "resumeChatDownload"
                    "retry" -> "retryChatDownload"
                    "remove" -> "removeChatDownload"
                    else -> error("Unknown chat download control")
                }
                "$field(id: ${gql(request.id)})"
            }
            Request.Snapshot -> error("Snapshot already handled")
        }
        return PlainRust.mainMutate(operation, longRunning = true).getValue(operation.substringBefore('('))
    }

    private sealed interface Request {
        data object Snapshot : Request
        data class Enqueue(val messageId: String, val fileId: String, val peerId: String) : Request
        data class Control(val id: String, val command: String) : Request
    }
}

@Serializable
private data class ChatDownloadSnapshot(val runtimeId: String, val revision: Long, val tasks: List<ChatDownloadRow>)

@Serializable
private data class ChatDownloadRow(
    val file: MessageFile,
    val peer: Peer,
    val messageId: String,
    val status: String,
    val error: String,
    val downloaded: Long,
    val speed: Long,
)

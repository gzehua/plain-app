package com.ismartcoding.plain.chat.download

import com.ismartcoding.plain.chat.models.MessageFile
import com.ismartcoding.plain.chat.models.PeerRecord
import com.ismartcoding.plain.chat.peer.RustPeerStore
import com.ismartcoding.plain.events.HDownloadTaskDoneEvent
import com.ismartcoding.plain.features.download.DOWNLOAD_KIND_CHAT
import com.ismartcoding.plain.features.download.DownloadCenter
import com.ismartcoding.plain.features.download.DownloadQueueVersion
import com.ismartcoding.plain.features.download.DownloadStatus
import com.ismartcoding.plain.graphql.models.Peer
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*

object DownloadQueue {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val intents = Channel<JsonObject>(128)
    private val stateLock = Mutex()
    private val version = DownloadQueueVersion()
    private val delivered = mutableSetOf<String>()
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
    fun addDownloadTask(messageFile: MessageFile, peer: PeerRecord, messageId: String): String {
        submit(buildJsonObject {
            put("action", "enqueue"); put("message_id", messageId); put("id", messageFile.id); put("peer_id", peer.id)
        })
        return messageFile.id
    }
    fun pauseDownload(taskId: String): Boolean = control(taskId, "pause")
    fun resumeDownload(taskId: String): Boolean = control(taskId, "resume")
    fun retryDownload(taskId: String): Boolean = control(taskId, "retry")
    fun removeDownload(taskId: String): Boolean = control(taskId, "remove")
    private fun control(id: String, command: String) = submit(buildJsonObject {
        put("action", "control"); put("id", id); put("command", command)
    })
    private fun submit(body: JsonObject): Boolean = intents.trySend(body).isSuccess.also { accepted ->
        if (!accepted) LogCat.e("Rust download request capacity exceeded")
    }

    suspend fun refresh() = stateLock.withLock {
        val snapshot = call(buildJsonObject { put("action", "snapshot") }).jsonObject
        val current = snapshot.getValue("revision").jsonPrimitive.long
        val runtime = snapshot.getValue("runtimeId").jsonPrimitive.content
        val reset = runtime != version.runtimeId
        if (!version.accept(runtime, current)) return@withLock
        if (reset) delivered.clear()
        val generations = mutableSetOf<String>()
        val tasks = snapshot.getValue("tasks").jsonArray.map { value ->
            val row = value.jsonObject
            val generation = row.getValue("generation").jsonPrimitive.content
            generations += generation
            val task = DownloadTask(
                messageFile = JsonHelper.jsonDecode<MessageFile>(row.getValue("file").toString()),
                peer = RustPeerStore.decode(row.getValue("peer")),
                messageId = row.getValue("messageId").jsonPrimitive.content,
                status = DownloadStatus.valueOf(row.getValue("status").jsonPrimitive.content),
                error = row.getValue("error").jsonPrimitive.content,
                downloadedSize = row.getValue("downloaded").jsonPrimitive.long,
                downloadSpeed = row.getValue("speed").jsonPrimitive.long,
            )
            if (task.status == DownloadStatus.COMPLETED) {
                if (delivered.add(generation)) sendEvent(HDownloadTaskDoneEvent(task))
                removeDownload(task.id)
            }
            task
        }.associateBy { it.id }
        delivered.retainAll(generations)
        downloadProgress.value = tasks
        DownloadCenter.replaceExternal(DOWNLOAD_KIND_CHAT, tasks.values.toList())
    }
    private const val QUEUE_FIELDS = "runtimeId revision tasks { id messageId generation status error downloaded total speed file { id: fileId uri size durationMs width height summary fileName } peer { id name ip key public_key: publicKey status port device_type: deviceType token created_at: createdAt updated_at: updatedAt } }"
    private suspend fun call(body: JsonObject): JsonElement {
        val action = body.getValue("action").jsonPrimitive.content
        if (action == "snapshot") return PlainRust.mainQuery("chatDownloadQueue { $QUEUE_FIELDS }").getValue("chatDownloadQueue")
        val operation = when (action) {
            "enqueue" -> "enqueueChatDownload(messageId: ${gql(body.getValue("message_id").jsonPrimitive.content)}, fileId: ${gql(body.getValue("id").jsonPrimitive.content)}, peerId: ${gql(body.getValue("peer_id").jsonPrimitive.content)})"
            "control" -> {
                val field = when (body.getValue("command").jsonPrimitive.content) {
                    "pause" -> "pauseChatDownload"
                    "resume" -> "resumeChatDownload"
                    "retry" -> "retryChatDownload"
                    "remove" -> "removeChatDownload"
                    else -> error("Unknown chat download control")
                }
                "$field(id: ${gql(body.getValue("id").jsonPrimitive.content)})"
            }
            else -> error("Unknown chat download operation: $action")
        }
        return PlainRust.mainMutate(operation, longRunning = true).getValue(operation.substringBefore('('))
    }
}

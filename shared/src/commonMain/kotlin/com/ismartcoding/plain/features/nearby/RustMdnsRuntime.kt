package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.rust.native.RustNativeApi
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.ui.nearby.NearbyViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*
import kotlinx.serialization.Serializable
import com.ismartcoding.plain.lib.JsonHelper
import kotlin.concurrent.Volatile

object RustMdnsRuntime {
    private class Command(val request: Request, val reply: CompletableDeferred<JsonObject>)
    private val commands = Channel<Command>(64)
    private val projection = Mutex()
    @Volatile private var current = MdnsRuntimeState(revision = -1)
    val running: Boolean get() = current.running
    val scanning: Boolean get() = current.scanning

    init {
        coIO {
            for (command in commands) {
                try {
                    val row = call(command.request)
                    apply(row)
                    command.reply.complete(row)
                } catch (cancelled: CancellationException) {
                    command.reply.cancel(cancelled)
                    throw cancelled
                } catch (error: Throwable) {
                    command.reply.completeExceptionally(error)
                    LogCat.e("Rust mDNS control", error)
                }
            }
        }
    }

    private fun enqueue(request: Request): CompletableDeferred<JsonObject> {
        val reply = CompletableDeferred<JsonObject>()
        if (!commands.trySend(Command(request, reply)).isSuccess) {
            val error = IllegalStateException("mDNS control queue is full")
            reply.completeExceptionally(error)
            LogCat.e("Rust mDNS control", error)
        }
        return reply
    }
    suspend fun debugStart(token: String) = enqueue(Request.DebugStart(token)).await()
    fun debugStop(token: String) = enqueue(Request.DebugStop(token))
    suspend fun snapshot(): JsonObject = RustGraphqlApi.graphql(Query.mdnsState()).getValue("mdnsState").jsonObject.also { apply(it) }

    private suspend fun call(request: Request): JsonObject = when (request) {
        is Request.DebugStart -> RustNativeApi.acquireMdnsDebugSession(request.token)
        is Request.DebugStop -> RustNativeApi.releaseMdnsDebugSession(request.token)
    }

    private sealed interface Request {
        data class DebugStart(val token: String) : Request
        data class DebugStop(val token: String) : Request
    }

    private suspend fun apply(row: JsonObject) = projection.withLock {
        val next = JsonHelper.jsonDecodeFromElement<MdnsRuntimeState>(row)
        if (next.runtimeId == current.runtimeId && next.revision < current.revision) return@withLock
        current = next
        NearbyViewModel.isDiscovering.value = scanning
    }
    suspend fun refresh() { snapshot() }
    fun services(row: JsonObject): List<MdnsServiceSnapshot> =
        JsonHelper.jsonDecodeFromElement<List<MdnsServiceRow>>(row.getValue("services")).map { service ->
            MdnsServiceSnapshot(service.serviceType, service.instanceName, service.instanceFqdn, service.hostname,
                service.port, service.txtRecords, service.ips + service.ipv6, service.complete)
        }
    fun packets(row: JsonObject, inbound: Boolean): List<MdnsPacketLog> =
        JsonHelper.jsonDecodeFromElement<List<MdnsPacketRow>>(row.getValue(if (inbound) "packetsIn" else "packetsOut")).map { packet ->
            MdnsPacketLog(packet.time, MdnsPacketDirection.valueOf(packet.direction), packet.srcIp, packet.srcPort,
                packet.dstIp, packet.dstPort, packet.size, packet.summary, packet.detail)
        }
}

@Serializable
private data class MdnsRuntimeState(
    val runtimeId: String? = null,
    val revision: Long,
    val running: Boolean = false,
    val scanning: Boolean = false,
)

@Serializable
private data class MdnsServiceRow(
    val serviceType: String,
    val instanceName: String,
    val instanceFqdn: String,
    val hostname: String,
    val port: Int,
    val txtRecords: List<String>,
    val ips: List<String>,
    val ipv6: List<String>,
    val complete: Boolean,
)

@Serializable
private data class MdnsPacketRow(
    val time: Long,
    val direction: String,
    val srcIp: String,
    val srcPort: Int,
    val dstIp: String,
    val dstPort: Int,
    val size: Int,
    val summary: String,
    val detail: String,
)

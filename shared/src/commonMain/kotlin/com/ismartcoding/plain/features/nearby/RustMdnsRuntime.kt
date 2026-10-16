package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.ui.nearby.NearbyViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*
import kotlin.concurrent.Volatile

object RustMdnsRuntime {
    private class Command(val request: Request, val reply: CompletableDeferred<JsonObject>)
    private val commands = Channel<Command>(64)
    private val projection = Mutex()
    @Volatile private var current = JsonObject(emptyMap())
    val running: Boolean get() = current["running"]?.jsonPrimitive?.booleanOrNull == true
    val scanning: Boolean get() = current["scanning"]?.jsonPrimitive?.booleanOrNull == true

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
    fun startReceiver() { enqueue(Request.Receiver) }
    suspend fun debugStart(token: String) = enqueue(Request.DebugStart(token)).await()
    fun debugStop(token: String) { enqueue(Request.DebugStop(token)) }
    suspend fun snapshot(): JsonObject = PlainRust.mainQuery("mdnsState { $STATE_FIELDS }").getValue("mdnsState").jsonObject.also { apply(it) }
    private const val STATE_FIELDS = "runtimeId revision receiver running scanning published manualScanning debugSessionCount capturing services { serviceType instanceName instanceFqdn hostname port txtRecords ips ipv6 complete } packetsIn { time direction srcIp srcPort dstIp dstPort size summary detail } packetsOut { time direction srcIp srcPort dstIp dstPort size summary detail }"
    private suspend fun call(request: Request): JsonObject {
        val operation = when (request) {
            Request.Receiver -> "startMdnsReceiver"
            is Request.DebugStart -> "acquireMdnsDebugSession(token: ${gql(request.token)})"
            is Request.DebugStop -> "releaseMdnsDebugSession(token: ${gql(request.token)})"
        }
        return PlainRust.mainMutate("$operation { $STATE_FIELDS }").getValue(operation.substringBefore('(')).jsonObject
    }

    private sealed interface Request {
        data object Receiver : Request
        data class DebugStart(val token: String) : Request
        data class DebugStop(val token: String) : Request
    }

    private suspend fun apply(row: JsonObject) = projection.withLock {
        val revision = row.getValue("revision").jsonPrimitive.long
        if (row["runtimeId"] == current["runtimeId"] && revision < (current["revision"]?.jsonPrimitive?.long ?: -1)) return@withLock
        current = row
        NearbyViewModel.isDiscovering.value = scanning
    }
    suspend fun refresh() { snapshot() }
    fun services(row: JsonObject): List<MdnsServiceSnapshot> = row.getValue("services").jsonArray.map {
        val service = it.jsonObject
        fun text(key: String) = service.getValue(key).jsonPrimitive.content
        fun strings(key: String) = service.getValue(key).jsonArray.map { value -> value.jsonPrimitive.content }
        MdnsServiceSnapshot(text("serviceType"), text("instanceName"), text("instanceFqdn"), text("hostname"), service.getValue("port").jsonPrimitive.int, strings("txtRecords"), strings("ips") + strings("ipv6"), service.getValue("complete").jsonPrimitive.boolean)
    }
    fun packets(row: JsonObject, inbound: Boolean): List<MdnsPacketLog> = row.getValue(if (inbound) "packetsIn" else "packetsOut").jsonArray.map {
        val packet = it.jsonObject
        fun text(key: String) = packet.getValue(key).jsonPrimitive.content
        fun int(key: String) = packet.getValue(key).jsonPrimitive.int
        MdnsPacketLog(packet.getValue("time").jsonPrimitive.long, MdnsPacketDirection.valueOf(text("direction")), text("srcIp"), int("srcPort"), text("dstIp"), int("dstPort"), int("size"), text("summary"), text("detail"))
    }
}

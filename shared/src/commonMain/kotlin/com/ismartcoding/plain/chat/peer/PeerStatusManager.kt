package com.ismartcoding.plain.chat.peer

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.logcat.LogCat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.*
import kotlin.concurrent.Volatile

object PeerStatusManager {
    @Volatile private var online = emptySet<String>()
    private val commands = Channel<String>(64)
    init {
        coIO {
            for (action in commands) {
                try {
                    PlainRust.mainMutate("controlPeerStatus(action: $action)")
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { LogCat.e("Rust peer status control", error) }
            }
        }
    }
    private fun request(action: String) {
        if (!commands.trySend(action).isSuccess) LogCat.e("Peer status control queue is full")
    }
    fun start() { request("START"); ensureAwareStarted() }
    fun stop() = request("STOP")
    fun reconnectNow(reason: String) { LogCat.d("Peer status reconnect: $reason"); request("RECONNECT") }
    fun ensureAwareStarted() = request("ENSURE_AWARE")
    fun isOnline(peerId: String): Boolean = peerId in online
    fun onlinePeers(): Set<String> = online

    /** Reads the live `peer.online` flag from the public `peers` GraphQL query. */
    suspend fun refresh() {
        val ids = PlainRust.mainQuery("peers { id online }").getValue("peers")
            .jsonArray
            .filter { it.jsonObject["online"]?.jsonPrimitive?.boolean == true }
            .map { it.jsonObject.getValue("id").jsonPrimitive.content }
            .toSet()
        val previous = online
        online = ids
        (previous + ids).forEach { id ->
            val active = id in ids
            if ((id in previous) != active) {
                PeerManager.setOnlineStatus(id, active)
            }
        }
    }

    /** Updates one peer's online state. Driven by the `PEER_CONNECTIONS_UPDATED` event. */
    internal fun applyOne(peerId: String, isOnline: Boolean) {
        val previous = online
        val next = if (isOnline) previous + peerId else previous - peerId
        online = next
        if ((peerId in previous) != isOnline) {
            PeerManager.setOnlineStatus(peerId, isOnline)
        }
    }
}
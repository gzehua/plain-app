package com.ismartcoding.plain.features.chat.peer

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.logcat.LogCat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel

object PeerStatusClient {
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
    fun reconnectNow(reason: String) { LogCat.d("Peer status reconnect: $reason"); request("RECONNECT") }
    fun ensureAwareStarted() = request("ENSURE_AWARE")
}

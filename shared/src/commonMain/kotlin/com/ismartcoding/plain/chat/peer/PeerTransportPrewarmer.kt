package com.ismartcoding.plain.chat.peer

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.lib.logcat.LogCat
import kotlinx.coroutines.*

object PeerTransportPrewarmer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun prewarm(peerId: String) {
        scope.launch {
            try {
                PlainRust.mainMutate("prewarmPeerTransport(id: ${gql(peerId)}) { shortId awareSupported awareRunning }", longRunning = true)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { LogCat.e("Peer prewarm", error) }
        }
    }
}

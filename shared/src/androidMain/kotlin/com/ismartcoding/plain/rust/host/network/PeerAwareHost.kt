package com.ismartcoding.plain.rust.host.network

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.network.AwareConfig
import com.ismartcoding.plain.platform.network.AwarePeer
import com.ismartcoding.plain.platform.network.startAwareIfNeeded
import com.ismartcoding.plain.platform.network.subscribeAwareForPeer
import com.ismartcoding.plain.platform.network.unsubscribeAwareForPeer
import com.ismartcoding.plain.platform.network.stopAware
import com.ismartcoding.plain.platform.system.PlatformLock
import com.ismartcoding.plain.rust.host.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

object PeerAwareHost {
    private val lock = PlatformLock()
    private var exchangeJob: Job? = null

    internal suspend fun handle(method: String, params: JsonObject, scope: CoroutineScope): HostResult {
        val job = scope.coroutineContext.job
        val attach = lock.withLock {
            if (exchangeJob === job) false else {
                exchangeJob = job
                true
            }
        }
        if (attach) job.invokeOnCompletion {
            lock.withLock {
                if (exchangeJob === job) {
                    exchangeJob = null
                    try { stopAware() } catch (error: Exception) { LogCat.e("Aware subscriptions cleanup", error) }
                    try { PeerSocketHost.disconnect() } catch (error: Exception) { LogCat.e("Peer socket cleanup", error) }
                }
            }
        }
        return when (method) {
            "peerStartAware" -> {
                val request = JsonHelper.jsonDecodeFromElement<StartAwareRequest>(params)
                val peer = request.peer
                val config = request.config
                PeerSocketHost.disconnectPeer(peer.id)
                val started = startAwareIfNeeded()
                if (started) subscribeAwareForPeer(peer, config) else unsubscribeAwareForPeer(peer.id)
                hostResult(started)
            }
            "peerStopAware" -> {
                val id = JsonHelper.jsonDecodeFromElement<IdRequest>(params).id
                PeerSocketHost.disconnectPeer(id)
                unsubscribeAwareForPeer(id)
                hostResult(true)
            }
            "peerTransportSocketOpen", "peerTransportSocketClose", "peerTransportSocketRead", "peerTransportSocketWrite",
            "peerTransportSocketCloseAll" -> PeerSocketHost.handle(method, params)
            else -> error("Unknown peer Host operation: $method")
        }
    }
}

@Serializable
private data class StartAwareRequest(val peer: AwarePeer, val config: AwareConfig)

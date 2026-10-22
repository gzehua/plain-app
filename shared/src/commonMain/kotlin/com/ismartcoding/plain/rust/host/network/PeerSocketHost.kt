package com.ismartcoding.plain.rust.host.network

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.network.openAwareByteSocket
import com.ismartcoding.plain.rust.host.*
import kotlin.io.encoding.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

object PeerSocketHost {
    @Serializable
    private data class ReadResult(val eof: Boolean, val bytes: String?)

    private val handles = PeerSocketHandles(::openAwareByteSocket)

    internal suspend fun handle(method: String, params: JsonObject): HostResult {
        if (method == "peerTransportSocketCloseAll") { disconnect(); return hostResult(true) }
        return when (method) {
            "peerTransportSocketOpen" -> {
                val request = JsonHelper.jsonDecodeFromElement<SocketOpenRequest>(params)
                handles.open(request.token, request.peerId)
                hostResult(true)
            }
            "peerTransportSocketClose" -> {
                val request = JsonHelper.jsonDecodeFromElement<SocketTokenRequest>(params)
                handles.close(request.token)
                hostResult(true)
            }
            "peerTransportSocketRead" -> {
                val request = JsonHelper.jsonDecodeFromElement<SocketReadRequest>(params)
                require(request.length in 1..32768)
                val bytes = handles.socket(request.token).read(request.length)
                hostResult(ReadResult(bytes != null && bytes.isEmpty(), bytes?.let { Base64.encode(it) }))
            }
            "peerTransportSocketWrite" -> {
                val request = JsonHelper.jsonDecodeFromElement<SocketWriteRequest>(params)
                val bytes = Base64.decode(request.bytes)
                require(bytes.size <= 32768)
                handles.socket(request.token).write(bytes)
                hostResult(bytes.size)
            }
            else -> error("Unknown socket operation")
        }
    }

    fun disconnectPeer(peerId: String) = handles.closePeer(peerId)

    fun disconnect() = handles.closeAll()
}

@Serializable
private data class SocketTokenRequest(val token: String)

@Serializable
private data class SocketOpenRequest(val token: String, val peerId: String)

@Serializable
private data class SocketReadRequest(val token: String, val length: Int)

@Serializable
private data class SocketWriteRequest(val token: String, val bytes: String)

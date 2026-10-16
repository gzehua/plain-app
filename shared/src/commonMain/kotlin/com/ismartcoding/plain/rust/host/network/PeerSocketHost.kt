package com.ismartcoding.plain.rust.host.network

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.network.openAwareByteSocket
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.io.encoding.Base64

object PeerSocketHost {
    @Serializable
    private data class ReadResult(val eof: Boolean, val bytes: String?)

    private val handles = PeerSocketHandles(::openAwareByteSocket)

    suspend fun handle(method: String, params: JsonObject): JsonElement {
        if (method == "peerTransportSocketCloseAll") { disconnect(); return JsonPrimitive(true) }
        val token = params.getValue("token").jsonPrimitive.content
        if (method == "peerTransportSocketOpen") {
            handles.open(token, params.getValue("peerId").jsonPrimitive.content)
            return JsonPrimitive(true)
        }
        if (method == "peerTransportSocketClose") { handles.close(token); return JsonPrimitive(true) }
        val socket = handles.socket(token)
        return when (method) {
            "peerTransportSocketRead" -> {
                val length = params.getValue("length").jsonPrimitive.int
                require(length in 1..32768)
                val bytes = socket.read(length)
                JsonHelper.jsonEncodeToElement(ReadResult(bytes != null && bytes.isEmpty(), bytes?.let { Base64.encode(it) }))
            }
            "peerTransportSocketWrite" -> {
                val bytes = Base64.decode(params.getValue("bytes").jsonPrimitive.content)
                require(bytes.size <= 32768)
                socket.write(bytes)
                JsonPrimitive(bytes.size)
            }
            else -> error("Unknown socket operation")
        }
    }
    fun disconnectPeer(peerId: String) = handles.closePeer(peerId)

    fun disconnect() = handles.closeAll()
}

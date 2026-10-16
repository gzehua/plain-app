package com.ismartcoding.plain.rust.host

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.bridge.RustCoreBridge
import com.ismartcoding.plain.rust.request.ContentApiSession
import com.ismartcoding.plain.features.images.index.ImageIndexHost
import com.ismartcoding.plain.features.audio.AudioLibraryHost
import com.ismartcoding.plain.features.audio.AudioEngineHost
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.files.handleMediaProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.serialization.json.*

internal object RustHostApi {
    private var collector: Job? = null

    fun start(scope: CoroutineScope, sessionProvider: () -> ContentApiSession) {
        check(collector == null) { "Rust host already started" }
        collector = scope.launch {
            try {
                coroutineScope {
                    val exchangeScope = this
                    val capacity = Semaphore(4)
                    for (text in PlainRust.hostRequests) {
                        val request = JsonHelper.jsonDecode<RustHostRequest>(text)
                        capacity.acquire()
                        launch {
                            try {
                                val session = sessionProvider()
                                val method = request.method
                                val params = request.params
                                val reply = try {
                                    val result = when {
                                        method == "fileResourceStream" -> { com.ismartcoding.plain.platform.files.FileResourceStreamHost.start(exchangeScope, session, params); JsonNull }
                                        method.startsWith("thumbnail") -> com.ismartcoding.plain.ui.media.thumbnail.ThumbnailHost.handle(session, method, params)
                                        method.startsWith("blePair") -> com.ismartcoding.plain.features.nearby.BlePairingHost.handle(method, params)
                                        method == "bleScanRead" -> com.ismartcoding.plain.features.nearby.BleScanDriver.read(params)
                                        method == "mdnsMulticast" -> com.ismartcoding.plain.features.nearby.MdnsMulticastHost.handle(params)
                                        method == "nearbyScanFacts" -> com.ismartcoding.plain.features.nearby.NearbyScanHost.facts()
                                        method == "pairingNotification" -> com.ismartcoding.plain.features.nearby.PairingNotificationHost.handle(params)
                                        method == "peerStartAware" -> com.ismartcoding.plain.features.chat.peer.PeerAwareHost.handle(method, params)
                                        method.startsWith("peerTransport") -> com.ismartcoding.plain.features.chat.peer.transport.PeerTransportHost.handle(method, params)
                                        com.ismartcoding.plain.rust.host.provider.SystemProviderHost.accepts(method) -> com.ismartcoding.plain.rust.host.provider.SystemProviderHost.handle(method, params)
                                        method == "audioEngineCommand" -> AudioEngineHost.handle(method, params)
                                        method.startsWith("fileTask") -> com.ismartcoding.plain.features.files.FileTaskHost.handle(method,params)
                                        (method == "mediaProviderRows" || method == "mediaProviderWrite") -> handleMediaProvider(method, params)
                                        method.startsWith("imageIndex") -> ImageIndexHost.handle(method, params)
                                        method == "audioLibraryResolve" -> AudioLibraryHost.handle(method, params)
                                        else -> error("Unknown Rust host operation: $method")
                                    }
                                    RustHostReply(request.id, result = result)
                                } catch (cancelled: CancellationException) { throw cancelled }
                                catch (error: Exception) { RustHostReply(request.id, error = error.message ?: "Host operation failed") }
                                val error = RustCoreBridge.send("reply", JsonHelper.jsonEncode(reply), byteArrayOf())
                                check(error.isEmpty()) { error }
                            } finally { capacity.release() }
                        }
                    }
                }
            } finally {
                withContext(NonCancellable) { disconnect() }
            }
        }
    }

    private suspend fun disconnect() {
        cleanup("Peer socket") { com.ismartcoding.plain.features.chat.peer.transport.PeerSocketHost.disconnect() }
        cleanup("Multicast permission") { com.ismartcoding.plain.features.nearby.MdnsMulticastHost.disconnect() }
        cleanup("BLE handle") { com.ismartcoding.plain.features.nearby.BlePairingHost.disconnect() }
        cleanup("Image index") { ImageIndexHost.disconnect() }
    }

    private suspend fun cleanup(name: String, block: suspend () -> Unit) {
        try { block() } catch (error: Exception) { LogCat.e("$name cleanup", error) }
    }
}

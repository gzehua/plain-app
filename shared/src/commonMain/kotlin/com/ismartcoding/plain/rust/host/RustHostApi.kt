package com.ismartcoding.plain.rust.host

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.bridge.RustCoreBridge
import com.ismartcoding.plain.rust.request.ContentApiSession
import com.ismartcoding.plain.features.imageindex.ImageIndexHost
import com.ismartcoding.plain.features.audio.AudioLibraryHost
import com.ismartcoding.plain.features.audio.AudioEngineHost
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.handleMediaActionHost
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
                                        method == "fileResourceStream" -> { com.ismartcoding.plain.platform.FileResourceStreamHost.start(exchangeScope, session, params); JsonNull }
                                        method.startsWith("thumbnail") -> com.ismartcoding.plain.thumbnail.ThumbnailHost.handle(session, method, params)
                                        method.startsWith("chatPicked") -> com.ismartcoding.plain.chat.ChatPickedHost.handle(method, params)
                                        method.startsWith("sharedTransfer") -> com.ismartcoding.plain.features.share.SharedTransferHost.handle(exchangeScope, method, params)
                                        method.startsWith("blePair") -> com.ismartcoding.plain.discover.BlePairingHost.handle(method, params)
                                        method == "bleScanRead" -> com.ismartcoding.plain.discover.BleScanDriver.read(params)
                                        method == "mainGraphqlHealth" || method == "mainGraphqlServerFailed" || method == "mainGraphqlShutdown" -> com.ismartcoding.plain.platform.HttpServerHost.handle(method, params)
                                        method == "mdnsMulticast" -> com.ismartcoding.plain.discover.MdnsMulticastHost.handle(params)
                                        method == "discoveryFacts" -> com.ismartcoding.plain.discover.DiscoveryAdvertisementHost.facts()
                                        method == "nearbyScanFacts" -> com.ismartcoding.plain.discover.NearbyScanHost.facts()
                                        method == "pairingNotification" -> com.ismartcoding.plain.discover.PairingNotificationHost.handle(params)
                                        method == "peerStartAware" || method == "peerDeviceInfo" -> com.ismartcoding.plain.chat.peer.PeerGraphQLHost.handle(method, params)
                                        method.startsWith("peerTransport") -> com.ismartcoding.plain.chat.peer.transport.PeerTransportHost.handle(method, params)
                                        method.startsWith("system") || method == "fileMetadataFacts" -> com.ismartcoding.plain.features.system.SystemProviderHost.handle(method, params)
                                        method == "audioEngineCommand" -> AudioEngineHost.handle(method, params)
                                        method.startsWith("fileTask") -> com.ismartcoding.plain.features.file.FileTaskHost.handle(method,params)
                                        method == "mediaAction" -> handleMediaActionHost(params)
                                        method.startsWith("imageIndex") -> ImageIndexHost.handle(method, params)
                                        else -> AudioLibraryHost.handle(method, params)
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
        cleanup("Peer socket") { com.ismartcoding.plain.chat.peer.transport.PeerSocketHost.disconnect() }
        cleanup("Selected file") { com.ismartcoding.plain.chat.ChatPickedHost.disconnect() }
        cleanup("Shared OS adapter") { com.ismartcoding.plain.features.share.SharedTransferHost.disconnect() }
        cleanup("Multicast permission") { com.ismartcoding.plain.discover.MdnsMulticastHost.disconnect() }
        cleanup("BLE handle") { com.ismartcoding.plain.discover.BlePairingHost.disconnect() }
        cleanup("Image index") { ImageIndexHost.disconnect() }
    }

    private suspend fun cleanup(name: String, block: suspend () -> Unit) {
        try { block() } catch (error: Exception) { LogCat.e("$name cleanup", error) }
    }
}

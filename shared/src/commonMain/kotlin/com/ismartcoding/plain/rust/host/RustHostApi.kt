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
                                    val result = when (routeForHostMethod(method)) {
                                        HostRoute.FileResourceStream -> { com.ismartcoding.plain.platform.FileResourceStreamHost.start(exchangeScope, session, params); JsonNull }
                                        HostRoute.Thumbnail -> com.ismartcoding.plain.thumbnail.ThumbnailHost.handle(session, method, params)
                                        HostRoute.ChatPicked -> com.ismartcoding.plain.chat.ChatPickedHost.handle(method, params)
                                        HostRoute.SharedTransfer -> com.ismartcoding.plain.features.share.SharedTransferHost.handle(exchangeScope, method, params)
                                        HostRoute.BlePairing -> com.ismartcoding.plain.discover.BlePairingHost.handle(method, params)
                                        HostRoute.BleScan -> com.ismartcoding.plain.discover.BleScanDriver.read(params)
                                        HostRoute.HttpServer -> com.ismartcoding.plain.platform.HttpServerHost.handle(method, params)
                                        HostRoute.MdnsMulticast -> com.ismartcoding.plain.discover.MdnsMulticastHost.handle(params)
                                        HostRoute.DiscoveryAdvertisement -> com.ismartcoding.plain.discover.DiscoveryAdvertisementHost.facts()
                                        HostRoute.NearbyScan -> com.ismartcoding.plain.discover.NearbyScanHost.facts()
                                        HostRoute.PairingNotification -> com.ismartcoding.plain.discover.PairingNotificationHost.handle(params)
                                        HostRoute.PeerGraphql -> com.ismartcoding.plain.chat.peer.PeerGraphQLHost.handle(method, params)
                                        HostRoute.PeerTransport -> com.ismartcoding.plain.chat.peer.transport.PeerTransportHost.handle(method, params)
                                        HostRoute.SystemProvider -> com.ismartcoding.plain.features.system.SystemProviderHost.handle(method, params)
                                        HostRoute.AudioEngine -> AudioEngineHost.handle(method, params)
                                        HostRoute.FileTask -> com.ismartcoding.plain.features.file.FileTaskHost.handle(method,params)
                                        HostRoute.MediaAction -> handleMediaActionHost(params)
                                        HostRoute.ImageIndex -> ImageIndexHost.handle(method, params)
                                        HostRoute.AudioLibrary -> AudioLibraryHost.handle(method, params)
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

package com.ismartcoding.plain.rust.host

import kotlinx.serialization.Serializable

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.bridge.RustCoreBridge
import com.ismartcoding.plain.rust.host.provider.ImageCatalogHost
import com.ismartcoding.plain.platform.media.audioLibraryResolve
import com.ismartcoding.plain.rust.host.media.AudioEngineHost
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.files.handleMediaProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.serialization.json.*

internal object RustHostApi {
    private var collector: Job? = null

    fun start(scope: CoroutineScope) {
        check(collector == null) { "Rust host already started" }
        collector = scope.launch {
            val bleAdvertising = com.ismartcoding.plain.platform.network.ble.PairingTransport.connect()
            val advertisingObservation = launch {
                try { bleAdvertising.observeFacts() }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { LogCat.e("BLE advertising observation", error) }
            }
            val observation = launch {
                try {
                    forwardAudioCatalogChanges(audioCatalogNotifications()) {
                        PlainRust.publish(com.ismartcoding.plain.app.events.HostEvent(
                            com.ismartcoding.plain.app.events.EventType.MEDIA_CATALOG_CHANGED,
                            JsonHelper.jsonEncode(MediaCatalogChange(listOf(com.ismartcoding.plain.platform.system.DataType.AUDIO.value))),
                        ))
                    }
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { LogCat.e("Audio catalog observation", error) }
            }
            try {
                coroutineScope {
                    val exchangeScope = this
                    val capacity = Semaphore(4)
                    for (text in PlainRust.hostRequests) {
                        val request = JsonHelper.jsonDecode<RustHostRequest>(text)
                        capacity.acquire()
                        launch {
                            try {
                                val method = request.method
                                val params = request.params
                                val reply = try {
                                    val result = when {
                                        method == "fileResourceStream" -> { com.ismartcoding.plain.platform.files.FileResourceStreamHost.start(exchangeScope, params); JsonNull }
                                        method == "thumbnailDecode" -> ThumbnailHost.decode(params)
                                        method.startsWith("blePair") -> com.ismartcoding.plain.rust.host.network.BlePairingHost.handle(method, params)
                                        method == "bleAdvertising" -> bleAdvertising.handle(params)
                                        method == "bleScanRead" -> com.ismartcoding.plain.features.nearby.BleScanDriver.read(params)
                                        method == "mdnsMulticast" -> com.ismartcoding.plain.rust.host.network.MdnsMulticastHost.handle(params)
                                        method == "nearbyScanFacts" -> com.ismartcoding.plain.rust.host.network.NearbyScanHost.facts()
                                        method == "pairingNotification" -> com.ismartcoding.plain.rust.host.network.PairingNotificationHost.handle(params)
                                        method == "peerStartAware" || method == "peerStopAware" -> com.ismartcoding.plain.rust.host.network.PeerAwareHost.handle(method, params)
                                        method.startsWith("peerTransport") -> com.ismartcoding.plain.rust.host.network.PeerTransportHost.handle(method, params)
                                        com.ismartcoding.plain.rust.host.provider.SystemProviderHost.accepts(method) -> com.ismartcoding.plain.rust.host.provider.SystemProviderHost.handle(method, params)
                                        method == "audioEngineCommand" -> AudioEngineHost.handle(method, params)
                                        method == "fileTaskScan" || method == "fileTaskMediaSnapshot" -> com.ismartcoding.plain.rust.host.provider.FileTaskHost.handle(method,params)
                                        (method == "mediaProviderRows" || method == "mediaProviderWrite") -> handleMediaProvider(method, params)
                                        method.startsWith("imageCatalog") -> ImageCatalogHost.handle(method, params)
                                        method == "audioLibraryResolve" -> {
                                            val input = JsonHelper.jsonDecodeFromElement<AudioLibraryResolveRequest>(params)
                                            JsonHelper.jsonEncodeToElement(audioLibraryResolve(input.ids, input.paths))
                                        }
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
                withContext(NonCancellable) {
                    observation.cancelAndJoin()
                    advertisingObservation.cancelAndJoin()
                    cleanup("BLE advertising") { bleAdvertising.close() }
                    disconnect()
                }
            }
        }
    }

    private suspend fun disconnect() {
        cleanup("Aware subscriptions") { com.ismartcoding.plain.platform.network.stopAware() }
        cleanup("Peer socket") { com.ismartcoding.plain.rust.host.network.PeerSocketHost.disconnect() }
        cleanup("Multicast permission") { com.ismartcoding.plain.rust.host.network.MdnsMulticastHost.disconnect() }
        cleanup("BLE handle") { com.ismartcoding.plain.rust.host.network.BlePairingHost.disconnect() }
        cleanup("Image index") { ImageCatalogHost.disconnect() }
    }

    private suspend fun cleanup(name: String, block: suspend () -> Unit) {
        try { block() } catch (error: Exception) { LogCat.e("$name cleanup", error) }
    }
}

@Serializable
private data class AudioLibraryResolveRequest(val ids: List<String>, val paths: List<String>)

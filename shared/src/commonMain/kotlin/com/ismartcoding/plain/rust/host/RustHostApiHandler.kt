package com.ismartcoding.plain.rust.host

import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HostEvent
import com.ismartcoding.plain.features.chat.peer.transport.PeerTransportType
import com.ismartcoding.plain.features.nearby.BleScanDriver
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.files.FileResourceHost
import com.ismartcoding.plain.platform.files.FileResourceStreamHost
import com.ismartcoding.plain.platform.files.handleMediaProvider
import com.ismartcoding.plain.platform.media.audioIsPlayingFlow
import com.ismartcoding.plain.platform.media.audioLibraryResolve
import com.ismartcoding.plain.platform.media.audioPlayerProgressAsync
import com.ismartcoding.plain.platform.media.disconnectImageCatalogHost
import com.ismartcoding.plain.platform.media.handleImageCatalogHost
import com.ismartcoding.plain.platform.media.observeImageCatalog
import com.ismartcoding.plain.platform.network.ble.PairingTransport
import com.ismartcoding.plain.platform.network.ble.PeerBleExchange
import com.ismartcoding.plain.platform.network.isBleReady
import com.ismartcoding.plain.platform.network.isWifiAwareSupported
import com.ismartcoding.plain.platform.network.stopAware
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.platform.system.deleteSystemProviderFacts
import com.ismartcoding.plain.preferences.PrefStore
import com.ismartcoding.plain.rust.host.audio.AudioEngineHost
import com.ismartcoding.plain.rust.host.audio.MediaCatalogChange
import com.ismartcoding.plain.rust.host.audio.audioCatalogNotifications
import com.ismartcoding.plain.rust.host.audio.forwardAudioCatalogChanges
import com.ismartcoding.plain.rust.host.files.FileTaskHost
import com.ismartcoding.plain.rust.host.files.SystemFilesHost
import com.ismartcoding.plain.rust.host.files.ThumbnailHost
import com.ismartcoding.plain.rust.host.network.BlePairingHost
import com.ismartcoding.plain.rust.host.network.MdnsMulticastHost
import com.ismartcoding.plain.rust.host.network.NearbyScanHost
import com.ismartcoding.plain.rust.host.network.PairingNotificationHost
import com.ismartcoding.plain.rust.host.network.PeerAwareHost
import com.ismartcoding.plain.rust.host.network.PeerSocketHost
import com.ismartcoding.plain.rust.host.network.PeerTransportPrewarmHost
import com.ismartcoding.plain.rust.host.provider.AudioPlaybackFacts
import com.ismartcoding.plain.rust.host.provider.SystemAppHost
import com.ismartcoding.plain.rust.host.provider.SystemCallsHost
import com.ismartcoding.plain.rust.host.provider.SystemContactsHost
import com.ismartcoding.plain.rust.host.provider.SystemMediaHost
import com.ismartcoding.plain.rust.host.provider.SystemNotificationsHost
import com.ismartcoding.plain.rust.host.provider.SystemPackagesHost
import com.ismartcoding.plain.rust.host.provider.SystemScreenMirrorHost
import com.ismartcoding.plain.rust.host.provider.SystemSmsHost
import com.ismartcoding.plain.rust.native.RustNativeApi
import com.ismartcoding.plain.rust.native.readRustStream
import com.ismartcoding.plain.rust.sendEventToRust
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Semaphore
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

internal object RustHostApiHandler {
    private val requests = Channel<String>(32)

    fun start(scope: CoroutineScope) {
        scope.launch {
            val bleAdvertising = PairingTransport.connect()
            val advertisingObservation = launch {
                try { bleAdvertising.observeFacts() }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { LogCat.e("BLE advertising observation", error) }
            }
            val observation = launch {
                try {
                    forwardAudioCatalogChanges(audioCatalogNotifications()) {
                        sendEventToRust(HostEvent(
                            EventType.MEDIA_CATALOG_CHANGED,
                            JsonHelper.jsonEncode(MediaCatalogChange(listOf(DataType.AUDIO.value))),
                        ))
                    }
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { LogCat.e("Audio catalog observation", error) }
            }
            val clipboardObservation = launch {
                try { observeClipboardReports() }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { LogCat.e("Clipboard observation", error) }
            }
            try {
                coroutineScope {
                    val exchangeScope = this
                    val capacity = Semaphore(4)
                    for (text in requests) {
                        val request = JsonHelper.jsonDecode<RustHostRequest>(text)
                        capacity.acquire()
                        launch {
                            try {
                                val reply = try {
                                    executeHostRequest(request, exchangeScope, bleAdvertising).encodeReply(request.id)
                                } catch (cancelled: CancellationException) { throw cancelled }
                                catch (error: Exception) { encodeHostError(request.id, error) }
                                val error = RustNativeApi.send("reply", reply, byteArrayOf())
                                check(error.isEmpty()) { error }
                            } finally { capacity.release() }
                        }
                    }
                }
            } finally {
                withContext(NonCancellable) {
                    clipboardObservation.cancelAndJoin()
                    observation.cancelAndJoin()
                    advertisingObservation.cancelAndJoin()
                    cleanup("BLE advertising") { bleAdvertising.close() }
                    disconnect()
                }
            }
        }
        scope.launch {
            readRustStream(
                next = { RustNativeApi.next("host") },
                consume = { requests.send(it) },
                closed = { requests.cancel() },
            )
        }
    }

    private suspend fun disconnect() {
        cleanup("Aware subscriptions") { stopAware() }
        cleanup("Peer socket") { PeerSocketHost.disconnect() }
        cleanup("Multicast permission") { MdnsMulticastHost.disconnect() }
        cleanup("BLE handle") { BlePairingHost.disconnect() }
        cleanup("Image index") { disconnectImageCatalogHost() }
    }

    private suspend fun cleanup(name: String, block: suspend () -> Unit) {
        try { block() } catch (error: Exception) { LogCat.e("$name cleanup", error) }
    }
}

internal suspend fun executeHostRequest(
    request: RustHostRequest,
    exchangeScope: CoroutineScope,
    bleAdvertising: PairingTransport.Connection,
): HostResult {
    val method = request.method
    val params = request.params
    return when (method) {
        "prefsChanged" -> {
            PrefStore.refresh()
            hostResult()
        }
        "fileResourceStream" -> { FileResourceStreamHost.start(exchangeScope, params); hostResult() }
        "thumbnailDecode" -> ThumbnailHost.decode(params)
        "blePairConnect", "blePairSend", "blePairWait",
        "blePairClose" -> BlePairingHost.handle(method, params)
        "bleAdvertising" -> hostResult(bleAdvertising.handle(params))
        "bleScanRead" -> hostResult(BleScanDriver.read(params))
        "mdnsMulticast" -> MdnsMulticastHost.handle(params)
        "nearbyScanFacts" -> NearbyScanHost.facts()
        "pairingNotification" -> PairingNotificationHost.handle(params)
        "peerStartAware",
        "peerStopAware" -> PeerAwareHost.handle(method, params)
        "peerTransportPrewarmScan" -> PeerTransportPrewarmHost.scan(params)
        "peerTransportCapabilities" -> hostResult(buildList {
            if (isWifiAwareSupported) add(PeerTransportType.AWARE)
            if (isBleReady()) add(PeerTransportType.BLE)
        })
        "peerTransportSocketOpen", "peerTransportSocketClose", "peerTransportSocketRead", "peerTransportSocketWrite",
        "peerTransportSocketCloseAll" -> PeerSocketHost.handle(method, params)
        "peerTransportBleExchange" -> hostResult(PeerBleExchange.exchange(params))
        "systemPackageFacts", "systemPackageStatuses", "systemInstallPackage", "systemPackageInstallProfile",
        "systemUninstallPackages" -> SystemPackagesHost.handle(method, params)
        "systemNotificationFacts", "systemCancelNotifications",
        "systemReplyNotification" -> SystemNotificationsHost.handle(method, params)
        "systemContactFacts", "systemContactCount", "systemContactIds", "systemContactGroupFacts",
        "systemContactSources", "systemCreateContact", "systemUpdateContact", "systemCreateContactGroup",
        "systemUpdateContactGroup",
        "systemDeleteContactGroup" -> SystemContactsHost.handle(method, params)
        "systemPhoneLocaleFacts", "systemCallFacts", "systemCallCount", "systemCallIds",
        "systemMakeCall" -> SystemCallsHost.handle(method, params)
        "systemMmsTextFacts", "systemSmsCountFacts", "systemSmsRowsFacts", "systemSmsIdsFacts",
        "systemSmsConversationFacts", "systemSmsThreadFacts", "systemDivideSmsText", "systemSendSmsParts",
        "systemDeleteSmsRows", "systemMmsLatest", "systemMmsLaunch", "systemMmsCandidates",
        "systemSimFacts" -> SystemSmsHost.handle(method, params)
        "systemScreenMirrorControls", "systemScreenMirrorResetTouch", "systemScreenMirrorState",
        "systemStartScreenMirror", "systemStopScreenMirror", "systemRequestScreenMirrorAudio",
        "systemRequestScreenMirrorKeyFrame" -> SystemScreenMirrorHost.handle(method, params)
        "systemImageModelsObserve" -> {
            observeImageCatalog(JsonHelper.jsonDecodeFromElement<ObserveImageCatalogRequest>(params).enabled)
            hostResult(true)
        }
        "systemFileResource" -> hostResult(FileResourceHost.handle(params))
        "systemFileIndexId", "systemFileIndexRows", "systemFileIndexCount", "systemMountFacts",
        "imageDecodeBounds" -> SystemFilesHost.handle(method, params)
        "systemMediaBucketItemFacts", "systemMediaRows", "systemMediaCount",
        "systemMediaIdsFacts" -> SystemMediaHost.handle(method, params)
        "systemAudioPlaybackState" -> hostResult(AudioPlaybackFacts(
            isPlaying = audioIsPlayingFlow().value,
            positionMs = audioPlayerProgressAsync(),
        ))
        "systemDeleteRecords" -> {
            val request = JsonHelper.jsonDecodeFromElement<DeleteRecordsRequest>(params)
            hostResult(deleteSystemProviderFacts(
                DataType.valueOf(request.provider), request.ids.toSet()))
        }
        "systemSetClipboard", "systemPermissionFacts", "systemOpenAccessibilitySettings", "systemOpenWebSettings",
        "systemNetworkType", "systemScheduleBackgroundWake", "systemProperties", "systemBatterySample",
        "systemCapabilities", "systemClearMediaCache",
        "systemRelaunchApp" -> SystemAppHost.handle(method, params)
        "audioEngineCommand" -> AudioEngineHost.command(params)
        "fileTaskScan",
        "fileTaskMediaSnapshot" -> FileTaskHost.handle(method,params)
        "mediaProviderRows",
        "mediaProviderWrite" -> hostResult(handleMediaProvider(method, params))
        "imageCatalogSnapshot", "imageCatalogPage", "imageCatalogResolve",
        "imageCatalogVerify" -> hostResult(handleImageCatalogHost(method, params))
        "audioLibraryResolve" -> {
            val input = JsonHelper.jsonDecodeFromElement<AudioLibraryResolveRequest>(params)
            hostResult(audioLibraryResolve(input.ids, input.paths))
        }
        else -> error("Unknown Rust host operation: $method")

    }
}

@Serializable
private data class ObserveImageCatalogRequest(val enabled: Boolean)

@Serializable
private data class DeleteRecordsRequest(val provider: String, val ids: List<String>)

@Serializable
private data class AudioLibraryResolveRequest(val ids: List<String>, val paths: List<String>)

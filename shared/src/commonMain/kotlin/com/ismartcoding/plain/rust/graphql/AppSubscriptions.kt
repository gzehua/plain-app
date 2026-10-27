package com.ismartcoding.plain.rust.graphql

import com.ismartcoding.plain.api.graphql.models.ChatItemId
import com.ismartcoding.plain.api.graphql.models.MediaLibraryChange
import com.ismartcoding.plain.api.graphql.models.MediaScanProgress
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.permissions.isGranted
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive

import com.ismartcoding.plain.api.graphql.models.*
import com.ismartcoding.plain.features.chat.channel.ChannelRuntimeEvent
import com.ismartcoding.plain.features.dlna.sender.CastSnapshot
import com.ismartcoding.plain.features.images.search.ImageModelsSnapshot
import com.ismartcoding.plain.features.settings.AppUpdateDownload
import com.ismartcoding.plain.models.PairingRequestInput

object AppSubscriptions {
    lateinit var contentChanged: SharedFlow<SubscriptionUpdate<Boolean>>
        private set
    lateinit var permissionsUpdated: SharedFlow<SubscriptionUpdate<Boolean>>
        private set
    lateinit var messagesCreated: SharedFlow<SubscriptionUpdate<List<ChatItemId>>>
        private set
    lateinit var messagesUpdated: SharedFlow<SubscriptionUpdate<List<ChatItemId>>>
        private set
    lateinit var messagesDeleted: SharedFlow<SubscriptionUpdate<String>>
        private set
    lateinit var mediaLibraryUpdated: SharedFlow<SubscriptionUpdate<MediaLibraryChange>>
        private set
    lateinit var mediaScanProgress: SharedFlow<SubscriptionUpdate<MediaScanProgress>>
        private set

    internal lateinit var networkChanged: SharedFlow<SubscriptionUpdate<Boolean>>
        private set
    internal lateinit var httpServerChanged: SharedFlow<SubscriptionUpdate<Boolean>>
        private set
    internal lateinit var audioLibraryChanged: SharedFlow<SubscriptionUpdate<Boolean>>
        private set
    internal lateinit var clipboardHistoryChanged: SharedFlow<SubscriptionUpdate<Boolean>>
        private set
    internal lateinit var peersChanged: SharedFlow<SubscriptionUpdate<Boolean>>
        private set
    internal lateinit var channelsChanged: SharedFlow<SubscriptionUpdate<ChannelRuntimeEvent>>
        private set
    internal lateinit var mdnsChanged: SharedFlow<SubscriptionUpdate<Boolean>>
        private set
    internal lateinit var nearbyDevicesChanged: SharedFlow<SubscriptionUpdate<Boolean>>
        private set
    internal lateinit var downloadsChanged: SharedFlow<SubscriptionUpdate<Boolean>>
        private set
    internal lateinit var sharedFolderDownloadsChanged: SharedFlow<SubscriptionUpdate<Boolean>>
        private set
    internal lateinit var dlnaRendererChanged: SharedFlow<SubscriptionUpdate<Boolean>>
        private set
    internal lateinit var dlnaSenderUpdated: SharedFlow<SubscriptionUpdate<CastSnapshot>>
        private set
    internal lateinit var peerTransportsUpdated: SharedFlow<SubscriptionUpdate<List<PeerTransportState>>>
        private set
    internal lateinit var onlineClientsUpdated: SharedFlow<SubscriptionUpdate<List<String>>>
        private set
    internal lateinit var imageModelsUpdated: SharedFlow<SubscriptionUpdate<ImageModelsSnapshot>>
        private set
    internal lateinit var appUpdateDownloadUpdated: SharedFlow<SubscriptionUpdate<AppUpdateDownload>>
        private set
    internal lateinit var feedSyncUpdated: SharedFlow<SubscriptionUpdate<FeedSyncUpdate>>
        private set
    internal lateinit var audioPlaybackChanged: SharedFlow<SubscriptionUpdate<AudioPlaybackChange>>
        private set
    internal lateinit var pairingChanged: SharedFlow<SubscriptionUpdate<Boolean>>
        private set
    internal lateinit var pairingRequested: SharedFlow<SubscriptionUpdate<PairingRequestInput>>
        private set
    internal lateinit var pairingSucceeded: SharedFlow<SubscriptionUpdate<PairingSuccessNotice>>
        private set
    internal lateinit var pomodoroAction: SharedFlow<SubscriptionUpdate<PomodoroAction>>
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    internal fun start(scope: CoroutineScope) {
        networkChanged = subscription<Boolean>("networkChanged", "").shareIn(scope, SharingStarted.WhileSubscribed())
        httpServerChanged = subscription<Boolean>("httpServerChanged", "").shareIn(scope, SharingStarted.WhileSubscribed())
        audioLibraryChanged = subscription<Boolean>("audioLibraryChanged", "").shareIn(scope, SharingStarted.WhileSubscribed())
        clipboardHistoryChanged = subscription<Boolean>("clipboardHistoryChanged", "").shareIn(scope, SharingStarted.WhileSubscribed())
        peersChanged = subscription<Boolean>("peersChanged", "").shareIn(scope, SharingStarted.WhileSubscribed())
        channelsChanged = subscription<ChannelRuntimeEvent>("channelsChanged", "{ changed invite { channelId ownerPeerId ownerPeerName } cancel { channelId ownerPeerId } }").shareIn(scope, SharingStarted.WhileSubscribed())
        mdnsChanged = subscription<Boolean>("mdnsChanged", "").shareIn(scope, SharingStarted.WhileSubscribed())
        nearbyDevicesChanged = subscription<Boolean>("nearbyDevicesChanged", "").shareIn(scope, SharingStarted.WhileSubscribed())
        downloadsChanged = subscription<Boolean>("downloadsChanged", "").shareIn(scope, SharingStarted.WhileSubscribed())
        sharedFolderDownloadsChanged = subscription<Boolean>("sharedFolderDownloadsChanged", "").shareIn(scope, SharingStarted.WhileSubscribed())
        dlnaRendererChanged = subscription<Boolean>("dlnaRendererChanged", "").shareIn(scope, SharingStarted.WhileSubscribed())
        dlnaSenderUpdated = subscription<CastSnapshot>("dlnaSenderUpdated", "{ version devices { id hostAddress name location } currentDevice { id hostAddress name location } items { path title albumArt artist durationMs audio } currentUri playing progressMs durationMs supportsCallback }").shareIn(scope, SharingStarted.WhileSubscribed())
        peerTransportsUpdated = subscription<List<PeerTransportState>>("peerTransportsUpdated", "{ peerId transport }").shareIn(scope, SharingStarted.WhileSubscribed())
        onlineClientsUpdated = subscription<List<String>>("onlineClientsUpdated", "").shareIn(scope, SharingStarted.WhileSubscribed())
        imageModelsUpdated = subscription<ImageModelsSnapshot>("imageModelsUpdated", "{ version status downloadProgress errorMessage modelSize modelDir isIndexing totalImages indexedImages }").shareIn(scope, SharingStarted.WhileSubscribed())
        appUpdateDownloadUpdated = subscription<AppUpdateDownload>("appUpdateDownloadUpdated", "{ coreId taskId revision status progress filePath error }").shareIn(scope, SharingStarted.WhileSubscribed())
        feedSyncUpdated = subscription<FeedSyncUpdate>("feedSyncUpdated", "{ states { feedId taskId revision status error } removed }").shareIn(scope, SharingStarted.WhileSubscribed())
        audioPlaybackChanged = subscription<AudioPlaybackChange>("audioPlaybackChanged", "{ revision positionMs }").shareIn(scope, SharingStarted.WhileSubscribed())
        pairingChanged = subscription<Boolean>("pairingChanged", "").shareIn(scope, SharingStarted.WhileSubscribed())
        pairingRequested = subscription<PairingRequestInput>("pairingRequested", "{ fromId fromName port deviceType ecdhPublicKey signaturePublicKey timestamp ips signature fromIp awareSupported }").shareIn(scope, SharingStarted.WhileSubscribed())
        pairingSucceeded = subscription<PairingSuccessNotice>("pairingSucceeded", "{ deviceId key }").shareIn(scope, SharingStarted.WhileSubscribed())
        pomodoroAction = subscription<PomodoroAction>("pomodoroAction", "{ today { date state timeLeftSec totalTimeSec completedCount currentRound isRunning isPaused } completedState }").shareIn(scope, SharingStarted.WhileSubscribed())
        contentChanged = subscription<Boolean>("contentChanged", "").shareIn(scope, SharingStarted.WhileSubscribed())
        permissionsUpdated = subscription<Boolean>("permissionsUpdated", "").shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)
        messagesCreated = subscription<List<ChatItemId>>("messagesCreated", "{ id }").shareIn(scope, SharingStarted.WhileSubscribed())
        messagesUpdated = subscription<List<ChatItemId>>("messagesUpdated", "{ id }").shareIn(scope, SharingStarted.WhileSubscribed())
        messagesDeleted = subscription<String>("messagesDeleted", "").shareIn(scope, SharingStarted.WhileSubscribed())
        val storageGranted = permissionsUpdated.changes().map { Permission.WRITE_EXTERNAL_STORAGE.isGranted() }.distinctUntilChanged()
        mediaLibraryUpdated = storageGranted.flatMapLatest { granted ->
            if (granted) subscription<MediaLibraryChange>("mediaLibraryUpdated", "{ types }") else emptyFlow()
        }.shareIn(scope, SharingStarted.WhileSubscribed())
        mediaScanProgress = storageGranted.flatMapLatest { granted ->
            if (granted) subscription<MediaScanProgress>("mediaScanProgress", "{ indexed pending total state error }") else emptyFlow()
        }.shareIn(scope, SharingStarted.WhileSubscribed())
    }

    private inline fun <reified T> subscription(field: String, selection: String): Flow<SubscriptionUpdate<T>> = flow {
        while (currentCoroutineContext().isActive) {
            try {
                RustGraphqlSubscriptions.subscribe(GraphqlRequest("subscription { $field $selection }")).collect { response ->
                    if (response.errors.isNotEmpty()) throw GraphqlSubscriptionException(response.errors)
                    if (response.extensions?.get("subscriptionReady")?.jsonPrimitive?.booleanOrNull == true) emit(SubscriptionUpdate.Attached)
                    response.data?.get(field)?.let { emit(SubscriptionUpdate.Data(JsonHelper.jsonDecodeFromElement<T>(it))) }
                }
                return@flow
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: GraphqlSubscriptionException) {
                if (!error.requiresResynchronization) { LogCat.e("GraphQL subscription $field", error); emit(SubscriptionUpdate.Failed(error)); return@flow }
            } catch (error: Exception) { LogCat.e("GraphQL subscription $field", error); emit(SubscriptionUpdate.Failed(error)); return@flow }
        }
    }
}

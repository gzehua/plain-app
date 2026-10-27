package com.ismartcoding.plain.rust.graphql

import com.ismartcoding.plain.api.graphql.models.ChatItemId
import com.ismartcoding.plain.api.graphql.models.MediaLibraryChange
import com.ismartcoding.plain.api.graphql.models.MediaScanProgress
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.permissions.isGranted
import com.ismartcoding.plain.rust.RustRuntime
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

@OptIn(ExperimentalCoroutinesApi::class)
object AppSubscriptions {
    private val scope = RustRuntime.scope
    internal val networkChanged =
        subscription<Boolean>("networkChanged")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val httpServerChanged =
        subscription<Boolean>("httpServerChanged")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val audioLibraryChanged =
        subscription<Boolean>("audioLibraryChanged")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val clipboardHistoryChanged =
        subscription<Boolean>("clipboardHistoryChanged")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val peersChanged =
        subscription<Boolean>("peersChanged")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val channelsChanged =
        subscription<ChannelRuntimeEvent>(
            "channelsChanged",
            "{ changed invite { channelId ownerPeerId ownerPeerName } cancel { channelId " +
                "ownerPeerId } }",
        )
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val mdnsChanged =
        subscription<Boolean>("mdnsChanged")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val nearbyDevicesChanged =
        subscription<Boolean>("nearbyDevicesChanged")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val downloadsChanged =
        subscription<Boolean>("downloadsChanged")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val sharedFolderDownloadsChanged =
        subscription<Boolean>("sharedFolderDownloadsChanged")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val dlnaRendererChanged =
        subscription<Boolean>("dlnaRendererChanged")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val dlnaSenderUpdated =
        subscription<CastSnapshot>(
            "dlnaSenderUpdated",
            "{ version devices { id hostAddress name location } currentDevice { id " +
                "hostAddress name location } items { path title albumArt artist durationMs " +
                "audio } currentUri playing progressMs durationMs supportsCallback }",
        )
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val peerTransportsUpdated =
        subscription<List<PeerTransportState>>("peerTransportsUpdated", "{ peerId transport }")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val onlineClientsUpdated =
        subscription<List<String>>("onlineClientsUpdated")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val imageModelsUpdated =
        subscription<ImageModelsSnapshot>(
            "imageModelsUpdated",
            "{ version status downloadProgress errorMessage modelSize modelDir " +
                "isIndexing totalImages indexedImages }",
        )
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val appUpdateDownloadUpdated =
        subscription<AppUpdateDownload>(
            "appUpdateDownloadUpdated",
            "{ coreId taskId revision status progress filePath error }",
        )
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val feedSyncUpdated =
        subscription<FeedSyncUpdate>(
            "feedSyncUpdated",
            "{ states { feedId taskId revision status error } removed }",
        )
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val audioPlaybackChanged =
        subscription<AudioPlaybackChange>("audioPlaybackChanged", "{ revision positionMs }")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val pairingChanged =
        subscription<Boolean>("pairingChanged")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val pairingRequested =
        subscription<PairingRequestInput>(
            "pairingRequested",
            "{ fromId fromName port deviceType ecdhPublicKey signaturePublicKey " +
                "timestamp ips signature fromIp awareSupported }",
        )
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val pairingSucceeded =
        subscription<PairingSuccessNotice>("pairingSucceeded", "{ deviceId key }")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    internal val pomodoroAction =
        subscription<PomodoroAction>(
            "pomodoroAction",
            "{ today { date state timeLeftSec totalTimeSec completedCount currentRound " +
                "isRunning isPaused } completedState }",
        )
            .shareIn(scope, SharingStarted.WhileSubscribed())
    val contentChanged =
        subscription<Boolean>("contentChanged")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    val permissionsUpdated =
        subscription<Boolean>("permissionsUpdated")
            .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)
    val messagesCreated =
        subscription<List<ChatItemId>>("messagesCreated", "{ id }")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    val messagesUpdated =
        subscription<List<ChatItemId>>("messagesUpdated", "{ id }")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    val messagesDeleted =
        subscription<String>("messagesDeleted")
            .shareIn(scope, SharingStarted.WhileSubscribed())
    private val storageGranted = permissionsUpdated.changes()
        .map { Permission.WRITE_EXTERNAL_STORAGE.isGranted() }
        .distinctUntilChanged()
    val mediaLibraryUpdated = storageGranted.flatMapLatest { granted ->
        if (granted) subscription<MediaLibraryChange>("mediaLibraryUpdated", "{ types }") else emptyFlow()
    }.shareIn(scope, SharingStarted.WhileSubscribed())
    val mediaScanProgress = storageGranted.flatMapLatest { granted ->
        if (granted) subscription<MediaScanProgress>("mediaScanProgress", "{ indexed pending total state error }") else emptyFlow()
    }.shareIn(scope, SharingStarted.WhileSubscribed())

    private inline fun <reified T> subscription(field: String, selection: String = ""): Flow<SubscriptionUpdate<T>> = flow {
        while (currentCoroutineContext().isActive) {
            try {
                RustGraphqlSubscriptions.subscribe(GraphqlRequest("subscription { $field $selection }")).collect { response ->
                    if (response.errors.isNotEmpty()) throw GraphqlSubscriptionException(response.errors)
                    if (response.extensions?.get("subscriptionReady")?.jsonPrimitive?.booleanOrNull == true) {
                        emit(SubscriptionUpdate.Attached)
                    }
                    response.data?.get(field)?.let {
                        emit(SubscriptionUpdate.Data(JsonHelper.jsonDecodeFromElement<T>(it)))
                    }
                }
                return@flow
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: GraphqlSubscriptionException) {
                if (!error.requiresResynchronization) {
                    LogCat.e("GraphQL subscription $field", error)
                    emit(SubscriptionUpdate.Failed(error))
                    return@flow
                }
            } catch (error: Exception) {
                LogCat.e("GraphQL subscription $field", error)
                emit(SubscriptionUpdate.Failed(error))
                return@flow
            }
        }
    }
}

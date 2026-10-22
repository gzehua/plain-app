package com.ismartcoding.plain.app.events

import com.ismartcoding.plain.models.PairingRequestInput
import com.ismartcoding.plain.ui.state.ActionSourceType
import com.ismartcoding.plain.ui.state.ActionType
import com.ismartcoding.plain.features.audio.AudioAction
import com.ismartcoding.plain.features.files.ExportFileType
import com.ismartcoding.plain.features.files.PickFileTag
import com.ismartcoding.plain.features.files.PickFileType
import com.ismartcoding.plain.features.bookmarks.BookmarksClient
import com.ismartcoding.plain.features.permissions.getGrantedWebPermissionsAsync
import com.ismartcoding.plain.features.bluetooth.BluetoothPermissionResultEvent
import com.ismartcoding.plain.features.feeds.FeedWorkerStatus
import com.ismartcoding.plain.lib.JsonHelper.jsonEncode
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.coMain
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.lib.ChannelEvent
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.media.audioPause
import com.ismartcoding.plain.features.settings.AppUpdateDownloads
import com.ismartcoding.plain.platform.media.restartAudioIfPlaying
import com.ismartcoding.plain.platform.notifications.sendChatMessageNotification
import com.ismartcoding.plain.platform.network.setBluetoothCanContinue
import com.ismartcoding.plain.app.startHttpServerService
import com.ismartcoding.plain.features.files.FolderOption
import com.ismartcoding.plain.features.sessions.AuthRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/** Native confirmation UI for a Rust-owned pending login. */
class HConfirmToAcceptLoginEvent(
    val clientId: String,
    val request: AuthRequest,
    val requestId: String,
    val clientIp: String,
) : HEvent()

data class HWebLoginClosedEvent(val requestId: String) : HEvent()

// Pairing events
data class HPairingRequestReceivedEvent(val request: PairingRequestInput) : HEvent()
data class HPairingSuccessEvent(val deviceId: String, val deviceName: String, val deviceIp: String, val key: String) : HEvent()
data class HPairingCanceledEvent(val fromId: String) : HEvent()

class FolderKanbanSelectEvent(val data: FolderOption) : ChannelEvent()

class StartHttpServerEvent : ChannelEvent()

/** Open the first-run service permission wizard (raised when a UI-initiated start is blocked on permissions). */
class ShowPermissionWizardEvent : ChannelEvent()


class RestartAppEvent : ChannelEvent()


class HPomodoroChangedEvent(val today: com.ismartcoding.plain.models.PomodoroToday) : HEvent()

class FetchBookmarkMetadataEvent(val bookmarkId: String, val url: String) : ChannelEvent()

class WindowFocusChangedEvent(val hasFocus: Boolean) : ChannelEvent()


/** Fired when a channel invite is received from a remote peer. UI shows accept/decline dialog. */
data class HChannelInviteReceivedEvent(
    val channelId: String,
    val channelName: String,
    val ownerPeerId: String,
    val ownerPeerName: String,
) : HEvent()

/** Fired when the channel owner cancels a pending invite (i.e. removes us before we accept).
 *  The auto-opened [com.ismartcoding.plain.ui.navigation.Routing.ChannelInviteRequest] page pops
 *  itself when it sees this event for the matching channel. */
data class HChannelInviteCanceledEvent(
    val channelId: String,
    val ownerPeerId: String,
) : HEvent()

class ExportFileEvent(val type: ExportFileType, val fileName: String) : ChannelEvent()

class ExportFileResultEvent(val type: ExportFileType, val uri: String) : ChannelEvent()

class PickFileEvent(val tag: PickFileTag, val type: PickFileType, val multiple: Boolean) : ChannelEvent()

class PickFileResultEvent(val tag: PickFileTag, val type: PickFileType, val uris: Set<String>) : ChannelEvent()

class HFeedStatusEvent(val feedId: String, val status: FeedWorkerStatus) : HEvent()

class ActionEvent(val source: ActionSourceType, val action: ActionType, val ids: Set<String>, val extra: Any? = null) : ChannelEvent()

class AudioActionEvent(val action: AudioAction) : ChannelEvent()

class IgnoreBatteryOptimizationEvent : ChannelEvent()
class PowerConnectedEvent : ChannelEvent()
class PowerDisconnectedEvent : ChannelEvent()
class HWebRequestReceivedEvent : HEvent()
data class KeepAwakeChangedEvent(val enabled: Boolean) : ChannelEvent()


class IgnoreBatteryOptimizationResultEvent : ChannelEvent()

class ClearAudioQueueEvent : ChannelEvent()

class DownloadUpdateEvent : ChannelEvent()
class CancelUpdateDownloadEvent : ChannelEvent()

class SleepTimerEvent(val durationMs: Long) : ChannelEvent()

class CancelSleepTimerEvent : ChannelEvent()


data class HChatMessageNotificationEvent(
    val targetId: String,
    val targetName: String,
    val messageText: String,
) : HEvent()

/**
 * Central event dispatcher for the app. Subscribes to [Channel.sharedFlow] and
 * dispatches each event to its handler. Pure business logic lives here; platform
 * specifics (audio service, notifications, HTTP server service, MMS polling,
 * APK download, image search, BLE) are delegated to `expect fun`s in
 * `commonMain/.../platform/` so this object has zero Android dependencies.
 *
 * Call [register] once at app startup. The collect loop runs on the Main
 * dispatcher; per-event work is dispatched to the IO dispatcher as needed.
 */
object AppEvents {
    private var sleepTimerJob: Job? = null

    fun register() {
        val sharedFlow = Channel.sharedFlow
        coMain {
            sharedFlow.collect { event ->
                when (event) {
                    is BluetoothPermissionResultEvent -> {
                        setBluetoothCanContinue(true)
                    }

                    is SleepTimerEvent -> {
                        sleepTimerJob?.cancel()
                        sleepTimerJob = coIO {
                            delay(event.durationMs.milliseconds)
                            audioPause()
                        }
                    }

                    is CancelSleepTimerEvent -> {
                        sleepTimerJob?.cancel()
                        sleepTimerJob = null
                    }

                    is FetchBookmarkMetadataEvent -> {
                        coIO {
                            BookmarksClient.fetchAndUpdateSingle(event.bookmarkId)
                        }
                    }

                    is HostEvent -> {
                        coIO {
                            com.ismartcoding.plain.rust.event.RustEventHandler.publish(event)
                        }
                    }

                    is PermissionsResultEvent -> {
                        coMain {
                            if (event.map.containsKey(Permission.POST_NOTIFICATIONS.toSysPermission())) {
                                restartAudioIfPlaying()
                            }
                        }
                        // Push the new permission snapshot to web clients so they
                        // refetch the app query.
                        coIO {
                            sendEvent(HostEvent(EventType.PERMISSIONS_UPDATED, jsonEncode(getGrantedWebPermissionsAsync())))
                        }
                    }

                    is StartHttpServerEvent -> {
                        startHttpServerService()
                    }

                    is DownloadUpdateEvent -> {
                        runUpdateDownloadCommand { AppUpdateDownloads.start() }
                    }

                    is CancelUpdateDownloadEvent -> {
                        runUpdateDownloadCommand { AppUpdateDownloads.cancel() }
                    }

                    is HChatMessageNotificationEvent -> {
                        sendChatMessageNotification(
                            targetId = event.targetId,
                            targetName = event.targetName,
                            messageText = event.messageText,
                        )
                    }

                }
            }
        }
    }
}

private fun runUpdateDownloadCommand(action: suspend () -> Unit) {
    coIO {
        try {
            action()
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            com.ismartcoding.plain.ui.components.DialogHelper.showErrorMessage(error.toString())
        }
    }
}

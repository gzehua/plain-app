package com.ismartcoding.plain.rust.event

import com.ismartcoding.plain.rust.request.ContentApiSession

import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HWebRequestReceivedEvent
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.request.RustContentApi
import com.ismartcoding.plain.ui.notes.NotesViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.*

internal object RustEventProjection {
    suspend fun reconcile() {
        com.ismartcoding.plain.features.videos.VideoProgressHost.restore()
        com.ismartcoding.plain.preferences.Prefs.refresh()
        com.ismartcoding.plain.features.sessions.refreshOnlineClientIds()
        com.ismartcoding.plain.features.chat.download.DownloadQueue.refresh()
        com.ismartcoding.plain.features.shares.SharedFolderDownloadEngine.refresh()
        com.ismartcoding.plain.features.nearby.PairingProjection.reconcile()
        val devices = com.ismartcoding.plain.features.nearby.RustNearbyDevices.snapshot()
        com.ismartcoding.plain.features.nearby.RustNearbyDevices.apply(devices)
        com.ismartcoding.plain.features.nearby.RustMdnsRuntime.refresh()
        com.ismartcoding.plain.features.dlna.DlnaRendererState.refresh()
        com.ismartcoding.plain.features.dlna.sender.RustDlnaSender.refresh()
        com.ismartcoding.plain.features.images.search.RustImageModels.refresh()
        com.ismartcoding.plain.features.chat.peer.PeerTransportProjection.refresh()
        com.ismartcoding.plain.features.chat.peer.PeerStatusManager.refresh()
        com.ismartcoding.plain.ui.chat.peer.PeerCacher.load()
        com.ismartcoding.plain.features.chat.channel.ChannelCacher.load()
        com.ismartcoding.plain.features.chat.ChatCacher.load()
    }

    suspend fun apply(message: RustEvent) {
        val type = message.type
        val payload = message.payload
        when (type) {
            EventType.IMAGE_INDEX_UPDATED.name -> com.ismartcoding.plain.features.images.search.RustImageModels.refresh()
            EventType.WEB_LOGIN_COMPLETED.name -> {
                val facts = JsonHelper.jsonDecode<com.ismartcoding.plain.features.sessions.WebLoginCompletedFacts>(payload)
                if (facts.chatPaired) com.ismartcoding.plain.features.chat.peer.PeerManager.load()
                val request = facts.request
                com.ismartcoding.plain.platform.notifications.sendWebLoginNotification(request.browserName, request.browserVersion, request.osName, request.osVersion, facts.clientIp)
            }
            EventType.WEB_SOCKET_REGISTERED.name -> com.ismartcoding.plain.app.onWebSocketSessionStarted()

            EventType.MEDIA_ACTION_FINISHED.name -> com.ismartcoding.plain.ui.media.actions.MediaActionProjection.apply(payload)
            EventType.HTTP_SERVER_STARTED.name -> {
                val started = JsonHelper.jsonDecode<com.ismartcoding.plain.rust.http.HttpServerStarted>(payload)
                com.ismartcoding.plain.rust.request.RustContentApi.httpStarted(started.generation, started.httpPort)
            }
            EventType.HTTP_SERVER_FAILED.name -> {
                val failure = JsonHelper.jsonDecode<com.ismartcoding.plain.rust.http.HttpServerFailure>(payload)
                com.ismartcoding.plain.app.onRustHttpServerFailed(failure.generation, failure.message)
            }
            EventType.PEER_STATUS_UPDATED.name -> {
                com.ismartcoding.plain.features.chat.peer.PeerStatusManager.refresh()
                com.ismartcoding.plain.ui.chat.peer.PeerCacher.load()
                com.ismartcoding.plain.features.chat.ChatCacher.load()
            }
            EventType.PEER_TRANSPORT_UPDATED.name -> com.ismartcoding.plain.features.chat.peer.PeerTransportProjection.refresh()
            EventType.PEER_CONNECTIONS_UPDATED.name -> {
                val obj = JsonHelper.jsonDecode<JsonObject>(payload)
                val id = obj["id"]?.jsonPrimitive?.contentOrNull
                val online = obj["online"]?.jsonPrimitive?.booleanOrNull
                if (id != null && online != null) {
                    com.ismartcoding.plain.features.chat.peer.PeerStatusManager.applyOne(id, online)
                } else {
                    com.ismartcoding.plain.features.chat.peer.PeerStatusManager.refresh()
                }
            }
            EventType.MDNS_UPDATED.name, EventType.NEARBY_DISCOVERY_STARTED.name, EventType.NEARBY_DISCOVERY_STOPPED.name -> com.ismartcoding.plain.features.nearby.RustMdnsRuntime.refresh(if (type == EventType.MDNS_UPDATED.name) payload else null)
            EventType.DLNA_RENDERER_UPDATED.name -> com.ismartcoding.plain.features.dlna.DlnaRendererState.apply(
                JsonHelper.jsonDecode<JsonObject>(payload).getValue("renderer").jsonObject)
            EventType.NEARBY_DEVICES_UPDATED.name -> {
                val devices = com.ismartcoding.plain.features.nearby.RustNearbyDevices.snapshot()
                com.ismartcoding.plain.features.nearby.RustNearbyDevices.apply(devices)
            }
            EventType.PAIRING_REQUEST_RECEIVED.name -> com.ismartcoding.plain.features.nearby.PairingProjection.request(payload)
            EventType.PAIRING_STARTED.name -> com.ismartcoding.plain.features.nearby.PairingProjection.started(payload)
            EventType.PAIRING_CANCELED.name -> com.ismartcoding.plain.features.nearby.PairingProjection.canceled(payload)
            EventType.PAIRING_SUCCESS.name -> com.ismartcoding.plain.features.nearby.PairingProjection.success(payload)
            EventType.PAIRING_FAILED.name -> com.ismartcoding.plain.features.nearby.PairingProjection.timeout(payload)
            EventType.MESSAGE_CREATED.name -> {
                JsonHelper.jsonDecode<JsonElement>(payload).jsonArray.forEach { value ->
                    com.ismartcoding.plain.features.chat.RustChatStore.getById(value.jsonObject.getValue("id").jsonPrimitive.content)?.let { item ->
                        if (item.fromId == "me") {
                            val target = if (item.channelId.isNullOrEmpty()) com.ismartcoding.plain.features.chat.data.ChatTarget.parseId("peer:" + item.toId)
                                else com.ismartcoding.plain.features.chat.data.ChatTarget.parseId("channel:" + item.channelId)
                            com.ismartcoding.plain.ui.chat.ChatViewModel.onMessagesCreated(target, listOf(item), scroll = true)
                            com.ismartcoding.plain.features.chat.ChatCacher.load()
                        } else com.ismartcoding.plain.features.chat.peer.RustPeerStore.getById(item.fromId)?.let { peer ->
                            val channel = item.channelId?.takeIf { it.isNotEmpty() }?.let { com.ismartcoding.plain.features.chat.channel.RustChannelStore.getById(it) }
                            com.ismartcoding.plain.features.chat.ChatMessageReceiver.applyCommitted(item, peer, channel)
                        }
                    }
                }
            }
            EventType.MESSAGE_DELETED.name -> {
                val target = JsonHelper.jsonDecode<JsonElement>(payload).jsonPrimitive.content
                if (target.startsWith("ids=")) com.ismartcoding.plain.ui.chat.ChatViewModel.onMessagesDeleted(target.removePrefix("ids=").split(',').filter { it.isNotEmpty() }.toSet())
                else com.ismartcoding.plain.ui.chat.ChatViewModel.onConversationCleared(target)
                com.ismartcoding.plain.features.chat.ChatCacher.load()
            }
            EventType.CHANNELS_UPDATED.name -> {
                val result = JsonHelper.jsonDecode<JsonElement>(payload).jsonObject
                com.ismartcoding.plain.features.chat.channel.ChannelSystemMessageReceiver.applyCommitted(result)
            }
            EventType.MESSAGE_UPDATED.name -> {
                val items = JsonHelper.jsonDecode<JsonElement>(payload).jsonArray.mapNotNull { value ->
                    com.ismartcoding.plain.features.chat.RustChatStore.getById(value.jsonObject.getValue("id").jsonPrimitive.content)
                }
                items.forEach { com.ismartcoding.plain.ui.chat.ChatViewModel.update(it) }
                com.ismartcoding.plain.features.chat.ChatCacher.load()
            }
            EventType.DOWNLOAD_PROGRESS.name -> {
                com.ismartcoding.plain.features.chat.download.DownloadQueue.refresh()
            }
            EventType.ONLINE_CLIENTS_UPDATED.name -> com.ismartcoding.plain.features.sessions.setOnlineClientIds(
                JsonHelper.jsonDecode<JsonElement>(payload).jsonArray.map { it.jsonPrimitive.content }.toSet())
            EventType.PREFS_UPDATED.name -> com.ismartcoding.plain.preferences.Prefs.refresh()
            EventType.IMAGE_MODELS_UPDATED.name -> com.ismartcoding.plain.features.images.search.RustImageModels.apply(JsonHelper.jsonDecode<JsonObject>(payload))
            EventType.DLNA_SENDER_UPDATED.name -> com.ismartcoding.plain.features.dlna.sender.RustDlnaSender.apply(JsonHelper.jsonDecode<JsonObject>(payload))
            EventType.WEB_LOGIN_REQUESTED.name -> {
                val facts = JsonHelper.jsonDecode<com.ismartcoding.plain.features.sessions.WebLoginRequestFacts>(payload)
                sendEvent(com.ismartcoding.plain.app.events.HConfirmToAcceptLoginEvent(
                    facts.clientId, facts.request, facts.requestId, facts.clientIp))
            }
            EventType.WEB_REQUEST_RECEIVED.name -> {
                sendEvent(HWebRequestReceivedEvent())
            }
            EventType.SHARED_FOLDER_DOWNLOAD_UPDATED.name -> com.ismartcoding.plain.features.shares.SharedFolderDownloadEngine.refresh()
            EventType.CONTENT_CHANGED.name -> {
                com.ismartcoding.plain.preferences.Prefs.refresh()
                com.ismartcoding.plain.ui.chat.peer.PeerCacher.load()
                com.ismartcoding.plain.features.chat.channel.ChannelCacher.load()
                com.ismartcoding.plain.features.chat.ChatClient.refreshLatestChats()
                try { com.ismartcoding.plain.features.files.FavoriteFoldersClient.refresh() }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { com.ismartcoding.plain.lib.logcat.LogCat.e("Favorite folders refresh",error) }
                RustContentApi.refreshSyncStates()
                NotesViewModel.reloadAsync()
                com.ismartcoding.plain.features.pomodoro.PomodoroHost.refresh()
            }
            EventType.POMODORO_ACTION.name -> {
                com.ismartcoding.plain.features.pomodoro.PomodoroHost.refresh()
            }
        }
    }
}

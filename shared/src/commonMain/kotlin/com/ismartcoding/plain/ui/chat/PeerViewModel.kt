package com.ismartcoding.plain.ui.chat

import com.ismartcoding.plain.preferences.PrefKeys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.features.chat.ChatCacher
import com.ismartcoding.plain.features.chat.peer.PeerManager
import com.ismartcoding.plain.features.chat.peer.RustPeerStore
import com.ismartcoding.plain.features.nearby.PeerStatus
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.models.ChatItem
import com.ismartcoding.plain.models.Peer
import com.ismartcoding.plain.preferences.Prefs
import com.ismartcoding.plain.rust.RustEventHandler
import com.ismartcoding.plain.ui.state.launchSafe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Instant

class PeerViewModel : ViewModel() {
    private val lock = Mutex()
    private val peers = MutableStateFlow<Map<String, Peer>>(emptyMap())
    val peersMap = peers.asStateFlow()
    val pairedPeers = combine(peers, ChatCacher.latestChatMap) { values, chats ->
        sortPeers(values.values.filter { it.isPaired() }, chats)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val unpairedPeers = combine(peers, ChatCacher.latestChatMap) { values, chats ->
        sortPeers(values.values.filter { it.status == PeerStatus.UNPAIRED }, chats)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // The consuming page owns this coroutine; hidden pages do not reload peers.
    suspend fun observe() = coroutineScope {
        launch(start = CoroutineStart.UNDISPATCHED) {
            RustEventHandler.events.collect { event ->
                if (isPeerUpdate(event.type)) refreshSafely()
            }
        }
        refreshSafely()
    }

    private suspend fun refresh() = lock.withLock {
        peers.value = RustPeerStore.getAll().associateBy { it.id }
    }

    private suspend fun refreshSafely() {
        try { refresh() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { LogCat.e("Load peers", error) }
    }

    fun updateDiscoverable(discoverable: Boolean) {
        viewModelScope.launchSafe {
            Prefs.set(PrefKeys.NEARBY_DISCOVERABLE, discoverable)
        }
    }

    fun removePeer(peerId: String) {
        viewModelScope.launchSafe {
            if (PeerManager.deletePeer(peerId)) refresh()
        }
    }

    fun load() {
        viewModelScope.launchSafe { refresh() }
    }

    private fun sortPeers(peers: List<Peer>, chats: Map<String, ChatItem>): List<Peer> = peers.sortedWith(
        compareByDescending<Peer> { chats[it.id]?.createdAt ?: Instant.DISTANT_PAST }
            .thenByDescending { it.online }
            .thenByDescending { it.createdAt }
            .thenBy { it.name.lowercase() },
    )
}

internal fun isPeerUpdate(type: EventType): Boolean = when (type) {
    EventType.PEER_UPDATED,
    EventType.PEER_STATUS_UPDATED,
    EventType.PEER_CONNECTIONS_UPDATED,
    EventType.CHANNELS_UPDATED,
    EventType.CONTENT_CHANGED,
    EventType.PAIRING_SUCCESS,
    EventType.WEB_LOGIN_COMPLETED -> true
    else -> false
}

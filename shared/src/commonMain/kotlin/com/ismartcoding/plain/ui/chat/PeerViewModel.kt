package com.ismartcoding.plain.ui.chat

import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.filter
import com.ismartcoding.plain.rust.graphql.AppSubscriptions
import com.ismartcoding.plain.rust.graphql.changes
import com.ismartcoding.plain.preferences.PrefKeys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismartcoding.plain.features.chat.RustChatStore
import com.ismartcoding.plain.features.chat.peer.PeerManager
import com.ismartcoding.plain.features.chat.peer.RustPeerStore
import com.ismartcoding.plain.features.nearby.PeerStatus
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.models.ChatItem
import com.ismartcoding.plain.models.Peer
import com.ismartcoding.plain.Prefs
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
    val latestChats = MutableStateFlow<Map<String, ChatItem>>(emptyMap())
    val peersMap = peers.asStateFlow()
    val pairedPeers = combine(peers, latestChats) { values, chats ->
        sortPeers(values.values.filter { it.isPaired() }, chats)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val unpairedPeers = combine(peers, latestChats) { values, chats ->
        sortPeers(values.values.filter { it.status == PeerStatus.UNPAIRED }, chats)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // The consuming page owns this coroutine; hidden pages do not reload peers.
    suspend fun observe() = coroutineScope {
        launch(start = CoroutineStart.UNDISPATCHED) {
            merge(
                AppSubscriptions.contentChanged.changes(),
                AppSubscriptions.messagesCreated.changes(),
                AppSubscriptions.messagesUpdated.changes(),
                AppSubscriptions.messagesDeleted.changes(),
                AppSubscriptions.peersChanged.changes(),
                AppSubscriptions.channelsChanged.changes(),
            ).collect { refreshSafely() }
        }
        refreshSafely()
    }

    private suspend fun refresh() = lock.withLock {
        peers.value = RustPeerStore.getAll().associateBy { it.id }
        latestChats.value = RustChatStore.getLatestConversations()
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
            PeerManager.deletePeer(peerId)
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

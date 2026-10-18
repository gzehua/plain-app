package com.ismartcoding.plain.ui.chat.peer

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable

import com.ismartcoding.plain.features.chat.peer.RustPeerStore

import com.ismartcoding.plain.features.chat.ChatCacher
import com.ismartcoding.plain.api.graphql.models.Peer
import com.ismartcoding.plain.features.chat.peer.transport.PeerTransportType
import com.ismartcoding.plain.features.nearby.PeerStatus
import com.ismartcoding.plain.api.graphql.models.ChatItem
import com.ismartcoding.plain.lib.withIO
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import com.ismartcoding.plain.rust.PlainRust
import kotlinx.serialization.json.*
import kotlinx.coroutines.flow.stateIn

object PeerCacher {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val peersMap = MutableStateFlow<Map<String, Peer>>(emptyMap())
    private val onlineMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    val currentTransportMap = MutableStateFlow<Map<String, PeerTransportType>>(emptyMap())

    val pairedPeers: StateFlow<List<Peer>> = combine(peersMap, ChatCacher.latestChatMap, onlineMap) { p, c, o ->
        sortPeers(p.values.filter { it.isPaired() }, c, o)
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val unpairedPeers: StateFlow<List<Peer>> = combine(peersMap, ChatCacher.latestChatMap, onlineMap) { p, c, o ->
        sortPeers(p.values.filter { it.status == PeerStatus.UNPAIRED }, c, o)
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val onlinePeerIds: StateFlow<Set<String>> = onlineMap
        .map { it.filterValues { online -> online }.keys.toSet() }
        .stateIn(scope, SharingStarted.Eagerly, emptySet())

    suspend fun refreshOnline() {
        val peers = JsonHelper.jsonDecodeFromElement<List<PeerOnlineState>>(PlainRust.mainQuery("peers { id online }").getValue("peers"))
        onlineMap.value = peers.associate { it.id to it.online }
    }

    fun setOnline(peerId: String, online: Boolean) {
        onlineMap.update { it + (peerId to online) }
    }

    suspend fun refreshPeer(peerId: String) = withIO {
        val peer = RustPeerStore.getById(peerId)
        peersMap.update { if (peer == null) it - peerId else it + (peerId to peer) }
    }

    fun isPeerOnline(peerId: String): Boolean = onlineMap.value[peerId] == true

    fun getPeerOnlineStatus(peerId: String): Boolean? = onlineMap.value[peerId]

    fun getOnlinePeerIds(): Set<String> = onlineMap.value.filterValues { it }.keys.toSet()

    fun getPeer(peerId: String): Peer? = peersMap.value[peerId]

    fun removePeer(peerId: String) {
        val currentPeers = peersMap.value
        val currentOnline = onlineMap.value
        val newPeers = if (currentPeers.containsKey(peerId)) currentPeers - peerId else currentPeers
        val newOnline = if (currentOnline.containsKey(peerId)) currentOnline - peerId else currentOnline
        if (newPeers !== currentPeers) peersMap.value = newPeers
        if (newOnline !== currentOnline) onlineMap.value = newOnline
        if (currentTransportMap.value.containsKey(peerId)) {
            currentTransportMap.value = currentTransportMap.value - peerId
        }
    }

    suspend fun load() = withIO {
        val peers = RustPeerStore.getAll()
        val runtimeMap = peers.associate { peer ->
            peer.id to peer
        }
        peersMap.value = runtimeMap
    }

    private fun sortPeers(
        peers: List<Peer>,
        chatCache: Map<String, ChatItem>,
        onlineMap: Map<String, Boolean>,
    ): List<Peer> {
        return peers.sortedWith(
            compareByDescending<Peer> { chatCache[it.id]?.createdAt ?: Instant.DISTANT_PAST }
                .thenByDescending { onlineMap[it.id] == true }
                .thenByDescending { it.createdAt }
                .thenBy { it.name.lowercase() },
        )
    }
}

@Serializable
private data class PeerOnlineState(val id: String, val online: Boolean)

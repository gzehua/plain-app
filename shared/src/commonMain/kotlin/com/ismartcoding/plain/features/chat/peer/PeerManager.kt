package com.ismartcoding.plain.features.chat.peer

import com.ismartcoding.plain.ui.chat.peer.PeerCacher

import com.ismartcoding.plain.features.chat.ChatCacher
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.lib.withIO

object PeerManager {
    suspend fun deletePeer(peerId: String): Boolean = withIO {
        if (!RustPeerStore.remove(peerId)) return@withIO false
        PeerCacher.removePeer(peerId)
        PeerCacher.load()
        ChatCacher.load()
        true
    }

    suspend fun markUnpaired(peerId: String): Boolean = withIO {
        if (!RustPeerStore.unpair(peerId)) return@withIO false
        PeerCacher.load()
        LogCat.d("Device unpaired: $peerId")
        true
    }

    suspend fun load() = withIO {
        PeerCacher.load()
        PeerCacher.refreshOnline()
    }
}

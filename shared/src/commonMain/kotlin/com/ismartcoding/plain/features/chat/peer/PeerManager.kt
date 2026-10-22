package com.ismartcoding.plain.features.chat.peer

import com.ismartcoding.plain.features.chat.ChatCacher
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.lib.withIO

object PeerManager {
    suspend fun deletePeer(peerId: String): Boolean = withIO {
        if (!RustPeerStore.remove(peerId)) return@withIO false
        ChatCacher.load()
        true
    }

    suspend fun markUnpaired(peerId: String): Boolean = withIO {
        if (!RustPeerStore.unpair(peerId)) return@withIO false
        LogCat.d("Device unpaired: $peerId")
        true
    }
}

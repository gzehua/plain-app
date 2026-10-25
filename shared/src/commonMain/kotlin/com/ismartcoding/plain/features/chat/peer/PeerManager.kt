package com.ismartcoding.plain.features.chat.peer

import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.lib.withIO

object PeerManager {
    suspend fun deletePeer(peerId: String): Boolean = RustPeerStore.remove(peerId)

    suspend fun markUnpaired(peerId: String): Boolean = withIO {
        if (!RustPeerStore.unpair(peerId)) return@withIO false
        LogCat.d("Device unpaired: $peerId")
        true
    }
}

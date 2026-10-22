package com.ismartcoding.plain.features.chat.peer

import com.ismartcoding.plain.rust.native.RustNativeApi

import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.logcat.LogCat
import kotlinx.coroutines.CancellationException

object PeerStatusClient {
    fun reconnectNow(reason: String) {
        LogCat.d("Peer status reconnect: $reason")
        coIO {
            try {
                RustNativeApi.reconnectPeers()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                LogCat.e("Rust peer status reconnect", error)
            }
        }
    }
}

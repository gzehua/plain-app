package com.ismartcoding.plain.platform.network

actual suspend fun openAwareByteSocket(peerId: String): PeerByteSocket = error("Wi-Fi Aware is unavailable on iOS")

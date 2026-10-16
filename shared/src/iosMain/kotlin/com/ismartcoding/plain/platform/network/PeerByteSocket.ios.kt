package com.ismartcoding.plain.platform.network

import com.ismartcoding.plain.features.chat.models.PeerRecord

actual suspend fun openAwareByteSocket(peer: PeerRecord): PeerByteSocket = error("Wi-Fi Aware is unavailable on iOS")

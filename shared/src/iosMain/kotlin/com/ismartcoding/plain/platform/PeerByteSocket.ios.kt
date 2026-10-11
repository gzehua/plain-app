package com.ismartcoding.plain.platform

import com.ismartcoding.plain.chat.models.PeerRecord

actual suspend fun openAwareByteSocket(peer: PeerRecord): PeerByteSocket = error("Wi-Fi Aware is unavailable on iOS")

package com.ismartcoding.plain.platform.network

import com.ismartcoding.plain.features.chat.models.PeerRecord
import com.ismartcoding.plain.features.chat.peer.transport.WifiAwareTransport

actual suspend fun openAwareByteSocket(peer: PeerRecord): PeerByteSocket = WifiAwareTransport.openSocket(peer)

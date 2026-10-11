package com.ismartcoding.plain.platform

import com.ismartcoding.plain.chat.models.PeerRecord
import com.ismartcoding.plain.chat.peer.transport.WifiAwareTransport

actual suspend fun openAwareByteSocket(peer: PeerRecord): PeerByteSocket = WifiAwareTransport.openSocket(peer)

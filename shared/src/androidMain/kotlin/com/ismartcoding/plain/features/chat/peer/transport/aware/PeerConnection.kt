package com.ismartcoding.plain.features.chat.peer.transport.aware

import android.net.Network
import java.net.Inet6Address

data class PeerConnection(
    val network: Network,
    val peerIpv6: Inet6Address,
    val peerPort: Int,
)

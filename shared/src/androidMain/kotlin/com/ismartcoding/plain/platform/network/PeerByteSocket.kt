package com.ismartcoding.plain.platform.network

import com.ismartcoding.plain.platform.network.aware.WifiAwareTransport

suspend fun openAwareByteSocket(peerId: String): PeerByteSocket = WifiAwareTransport.openSocket(peerId)

package com.ismartcoding.plain.platform.network

import com.ismartcoding.plain.features.chat.models.PeerRecord

interface PeerByteSocket {
    suspend fun read(length: Int): ByteArray?
    suspend fun write(bytes: ByteArray)
    fun close()
}

expect suspend fun openAwareByteSocket(peer: PeerRecord): PeerByteSocket

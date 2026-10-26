package com.ismartcoding.plain.platform.network

interface PeerByteSocket {
    suspend fun read(length: Int): ByteArray?
    suspend fun write(bytes: ByteArray)
    fun close()
}

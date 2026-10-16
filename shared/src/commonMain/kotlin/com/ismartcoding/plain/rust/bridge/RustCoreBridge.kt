package com.ismartcoding.plain.rust.bridge

internal expect object RustCoreBridge {
    fun start(dataDir: String, token: String, configJson: String): String
    fun graphql(request: String): String
    fun next(stream: String): String
    fun send(action: String, message: String, bytes: ByteArray): String
    fun uploadChunk(id: String, index: Long, bytes: ByteArray): String
    fun resourceOpen(id: String): String
    fun resourceWrite(id: String, bytes: ByteArray): String
    fun resourceClose(id: String, failed: Boolean): String
    fun stop()
}

package com.ismartcoding.plain.rust.bridge

internal expect object RustCoreBridge {
    fun start(databasePath: String, token: String, configJson: String): String
    fun graphql(request: String): String
    fun hostGraphql(request: String): String
    fun next(stream: String): String
    fun send(action: String, message: String, bytes: ByteArray): String
    fun resourceOpen(id: String): String
    fun resourceWrite(id: String, bytes: ByteArray): String
    fun resourceClose(id: String, failed: Boolean): String
    fun archiveOpen(entries: String): String
    fun archiveRead(handle: ULong, buffer: ByteArray): String
    fun archiveClose(handle: ULong): String
    fun stop()
}

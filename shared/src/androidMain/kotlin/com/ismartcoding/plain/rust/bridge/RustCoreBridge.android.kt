package com.ismartcoding.plain.rust.bridge

internal actual object RustCoreBridge {
    init { System.loadLibrary("plain_rust") }
    private external fun startNative(databasePath: String, token: String, configJson: String): String
    private external fun stopNative(): String
    actual fun start(databasePath: String, token: String, configJson: String): String = startNative(databasePath, token, configJson)
    private external fun graphqlNative(request: String): String
    actual fun graphql(request: String): String = graphqlNative(request)
    private external fun nextNative(stream: String): String
    private external fun sendNative(action: String, message: String, bytes: ByteArray): String
    actual fun next(stream: String): String = nextNative(stream)
    actual fun send(action: String, message: String, bytes: ByteArray): String = sendNative(action, message, bytes)
    private external fun resourceOpenNative(id: String): String
    private external fun resourceWriteNative(id: String, bytes: ByteArray): String
    private external fun resourceCloseNative(id: String, failed: Boolean): String
    actual fun resourceOpen(id: String): String = resourceOpenNative(id)
    actual fun resourceWrite(id: String, bytes: ByteArray): String = resourceWriteNative(id, bytes)
    actual fun resourceClose(id: String, failed: Boolean): String = resourceCloseNative(id, failed)
    private external fun archiveOpenNative(entries: String): String
    private external fun archiveReadNative(handle: Long, buffer: ByteArray): String
    private external fun archiveCloseNative(handle: Long): String
    actual fun archiveOpen(entries: String): String = archiveOpenNative(entries)
    actual fun archiveRead(handle: ULong, buffer: ByteArray): String = archiveReadNative(handle.toLong(), buffer)
    actual fun archiveClose(handle: ULong): String = archiveCloseNative(handle.toLong())
    actual fun stop() { val error = stopNative(); check(error.isEmpty()) { error } }
}

package com.ismartcoding.plain.rust.bridge

internal actual object RustCoreBridge {
    init { System.loadLibrary("plain_rust") }
    private external fun startNative(dataDir: String, token: String, configJson: String): String
    private external fun stopNative(): String
    actual fun start(dataDir: String, token: String, configJson: String): String = startNative(dataDir, token, configJson)
    private external fun graphqlNative(request: String): String
    actual fun graphql(request: String): String = graphqlNative(request)
    private external fun nextNative(stream: String): String
    private external fun sendNative(action: String, message: String, bytes: ByteArray): String
    actual fun next(stream: String): String = nextNative(stream)
    actual fun send(action: String, message: String, bytes: ByteArray): String = sendNative(action, message, bytes)
    private external fun uploadChunkNative(id: String, index: Long, bytes: ByteArray): String
    actual fun uploadChunk(id: String, index: Long, bytes: ByteArray): String = uploadChunkNative(id, index, bytes)
    private external fun resourceOpenNative(id: String): String
    private external fun resourceWriteNative(id: String, bytes: ByteArray): String
    private external fun resourceFileNative(id: String, fd: Int, offset: Long, length: Long): String
    fun resourceFile(id: String, fd: Int, offset: Long, length: Long): String = resourceFileNative(id, fd, offset, length)
    private external fun resourceCloseNative(id: String, failed: Boolean): String
    actual fun resourceOpen(id: String): String = resourceOpenNative(id)
    actual fun resourceWrite(id: String, bytes: ByteArray): String = resourceWriteNative(id, bytes)
    actual fun resourceClose(id: String, failed: Boolean): String = resourceCloseNative(id, failed)
    actual fun stop() { val error = stopNative(); check(error.isEmpty()) { error } }
}

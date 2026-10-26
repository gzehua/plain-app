package com.ismartcoding.plain.platform.files

import com.ismartcoding.plain.rust.native.RustNativeBindings

internal class NativeResourceStreamSink(private val id: String) : StreamSink {
    private var closed = false
    override suspend fun write(bytes: ByteArray) = write(bytes, 0, bytes.size)
    override suspend fun write(bytes: ByteArray, offset: Int, length: Int) {
        check(!closed) { "Native resource sink is closed" }
        require(offset >= 0 && length >= 0 && offset <= bytes.size - length)
        var current = offset
        while (current < offset + length) {
            val end = minOf(current + 64 * 1024, offset + length)
            val error = RustNativeBindings.resourceWrite(id, bytes.copyOfRange(current, end))
            check(error.isEmpty()) { error }
            current = end
        }
    }
    override suspend fun flush() {}
    override suspend fun close() { closed = true }
}

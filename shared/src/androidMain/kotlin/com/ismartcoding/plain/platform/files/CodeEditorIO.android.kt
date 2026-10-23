package com.ismartcoding.plain.platform.files

import android.net.Uri
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.ui.components.codeeditor.engine.ByteSource
import com.ismartcoding.plain.ui.components.codeeditor.engine.ChunkedByteSource
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import android.os.ParcelFileDescriptor

actual fun openByteSource(path: String): ByteSource {
    val channel: FileChannel = if (path.startsWith("content://")) {
        val pfd = appContext.contentResolver.openFileDescriptor(Uri.parse(path), "r")
            ?: throw IllegalArgumentException("cannot open $path")
        try {
            ParcelFileDescriptor.AutoCloseInputStream(pfd).channel
        } catch (e: Exception) {
            runCatching { pfd.close() }
            throw e
        }
    } else {
        val filePath = if (path.startsWith("file://")) Uri.parse(path).path else path
        FileInputStream(File(filePath ?: path)).channel
    }
    val source = object : ByteSource {
        override val size: Long = channel.size()

        @Synchronized
        override fun readAt(offset: Long, length: Int): ByteArray {
            val buffer = ByteBuffer.allocate(length)
            var read = 0
            while (read < length) {
                val n = channel.read(buffer, offset + read)
                if (n < 0) break
                read += n
            }
            if (read < length) throw IllegalStateException("short read at $offset")
            return buffer.array()
        }

        override fun close() {
            runCatching { channel.close() }
        }
    }
    return ChunkedByteSource(source)
}

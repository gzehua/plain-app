package com.ismartcoding.plain.platform.files


import com.ismartcoding.plain.ui.components.codeeditor.engine.ByteSource
import com.ismartcoding.plain.ui.components.codeeditor.engine.ChunkedByteSource
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toLong
import kotlinx.cinterop.usePinned
import platform.posix.O_RDONLY
import platform.posix.close
import platform.posix.fstat
import platform.posix.open
import platform.posix.pread
import platform.posix.stat

@OptIn(ExperimentalForeignApi::class)
actual fun openByteSource(path: String): ByteSource {
    val fd = open(path, O_RDONLY)
    if (fd < 0) throw IllegalArgumentException("cannot open $path")
    val fileSize = memScoped {
        val st = alloc<stat>()
        if (fstat(fd, st.ptr) != 0) {
            close(fd)
            throw IllegalArgumentException("cannot stat $path")
        }
        st.st_size
    }
    val source = object : SynchronizedObject(), ByteSource {
        override val size: Long = fileSize

        override fun readAt(offset: Long, length: Int): ByteArray = synchronized(this) {
            val buffer = ByteArray(length)
            if (length > 0) {
                buffer.usePinned { pinned ->
                    var done = 0
                    while (done < length) {
                        val n = pread(fd, pinned.addressOf(done), (length - done).toULong(), offset + done)
                        if (n <= 0) throw IllegalStateException("short read at $offset")
                        done += n.toInt()
                    }
                }
            }
            buffer
        }

        override fun close() {
            close(fd)
        }
    }
    return ChunkedByteSource(source)
}

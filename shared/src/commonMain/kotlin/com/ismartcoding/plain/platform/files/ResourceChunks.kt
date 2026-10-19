package com.ismartcoding.plain.platform.files

import com.ismartcoding.plain.platform.system.PlatformLock
import kotlinx.coroutines.suspendCancellableCoroutine

internal suspend fun <T> readResourceChunks(
    start: ((T) -> Unit, (Throwable?) -> Unit) -> Int,
    cancel: (Int) -> Unit,
    size: (T) -> Long,
    write: (T) -> Unit,
): Long = suspendCancellableCoroutine { continuation ->
    val lock = PlatformLock()
    var accepting = true
    var completed = false
    var length = 0L
    var requestId: Int? = null
    var cancellationRequested = false
    var cancellationSent = false

    fun finish(error: Throwable?) {
        val result = lock.withLock {
            if (completed) return@withLock null
            completed = true
            accepting = false
            if (error == null) Result.success(length) else Result.failure<Long>(error)
        }
        result?.let(continuation::resumeWith)
    }

    fun cancelRequest() {
        val id = lock.withLock {
            if (!cancellationRequested || cancellationSent) return@withLock null
            requestId?.also { cancellationSent = true }
        }
        id?.let { runCatching { cancel(it) }.exceptionOrNull()?.let(::finish) }
    }

    continuation.invokeOnCancellation {
        lock.withLock {
            accepting = false
            if (!completed) {
                completed = true
                cancellationRequested = true
            }
        }
        cancelRequest()
    }
    if (!continuation.isActive) return@suspendCancellableCoroutine
    try {
        val id = start({ chunk ->
            val failure = lock.withLock {
                if (!accepting) return@withLock null
                runCatching {
                    val bytes = size(chunk)
                    check(bytes >= 0 && bytes <= Long.MAX_VALUE - length) { "Resource length overflow" }
                    write(chunk)
                    length += bytes
                }.exceptionOrNull()?.also {
                    accepting = false
                    cancellationRequested = true
                }
            }
            if (failure != null) {
                finish(failure)
                cancelRequest()
            }
        }, ::finish)
        check(id > 0) { "Resource request is unavailable" }
        lock.withLock { requestId = id }
        cancelRequest()
    } catch (error: Throwable) {
        finish(error)
    }
}

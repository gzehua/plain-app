package com.ismartcoding.plain.platform.system

import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

internal suspend fun <T> awaitPlatformFuture(future: ListenableFuture<T>, executor: Executor, dispose: (T) -> Unit): T =
    suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { future.cancel(false) }
        future.addListener({
            val value = try {
                future.get()
            } catch (error: Exception) {
                continuation.resumeWithException(if (error is ExecutionException) error.cause ?: error else error)
                return@addListener
            }
            continuation.resume(value) { _, rejected, _ -> executor.execute { dispose(rejected) } }
        }, executor)
    }

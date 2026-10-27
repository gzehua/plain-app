package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.rust.RustRuntime
import com.ismartcoding.plain.rust.native.RustNativeApi
import kotlinx.coroutines.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object BackgroundExecution {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = PlatformLock()
    private val jobs = mutableMapOf<String, Job>()
    private val completions = mutableMapOf<String, CompletableDeferred<Boolean>>()

    @OptIn(ExperimentalUuidApi::class)
    fun begin(deadlineMs: Long, completed: (Boolean) -> Unit): String {
        val id = Uuid.random().toString()
        launchExecution(id, scope, completed) { run(deadlineMs, id) }
        return id
    }

    internal fun launchExecution(id: String, executionScope: CoroutineScope, completed: (Boolean) -> Unit, work: suspend () -> Boolean): Job {
        var success = false
        val job = lock.withLock {
            check(id !in jobs) { "Background execution ID already active" }
            executionScope.launch(start = CoroutineStart.LAZY) {
                val result = try { work() }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { LogCat.e("Background execution failed", error); false }
                lock.withLock { success = result }
            }.also { jobs[id] = it }
        }
        job.invokeOnCompletion { cause ->
            val result = lock.withLock { jobs.remove(id); cause == null && success }
            completed(result)
        }
        job.start()
        return job
    }

    fun cancel(id: String) { lock.withLock { jobs[id] }?.cancel() }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun run(deadlineMs: Long, id: String = Uuid.random().toString()): Boolean {
        RustRuntime.start()
        val finished = CompletableDeferred<Boolean>()
        lock.withLock {
            check(id !in completions) { "Background execution ID already active" }
            completions[id] = finished
        }
        return try {
            withGrant(
                grant = { RustNativeApi.grantBackgroundExecution(id, deadlineMs) },
                awaitCompletion = { finished.await() },
                revoke = { RustNativeApi.revokeBackgroundExecution(id) },
            )
        } finally {
            lock.withLock { completions.remove(id) }
        }
    }

    internal fun complete(id: String, success: Boolean) {
        lock.withLock { completions[id] }?.complete(success)
    }

    internal suspend fun withGrant(grant: suspend () -> Unit, awaitCompletion: suspend () -> Boolean, revoke: suspend () -> Unit): Boolean {
        try {
            withContext(NonCancellable) { grant() }
            currentCoroutineContext().ensureActive()
            return awaitCompletion()
        } finally {
            withContext(NonCancellable) {
                try { revoke() }
                catch (error: Exception) { LogCat.e("Background execution release failed", error) }
            }
        }
    }

    fun executionStateChanged() {
        if (!RustRuntime.isCoreInitialized) return
        scope.launch {
            try { RustNativeApi.executionStateChanged() }
            catch (error: Exception) { LogCat.e("Execution state report failed", error) }
        }
    }
}

internal expect suspend fun scheduleBackgroundWake(atMs: Long?)

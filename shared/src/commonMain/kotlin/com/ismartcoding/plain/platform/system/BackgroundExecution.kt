package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.rust.RustEventHandler

import kotlinx.serialization.Serializable

import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HostEvent
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.IODispatcher
import com.ismartcoding.plain.preferences.Prefs
import com.ismartcoding.plain.rust.RustRuntime
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object BackgroundExecution {
    private val scope = CoroutineScope(SupervisorJob() + IODispatcher)
    private val lock = PlatformLock()
    private val jobs = mutableMapOf<String, Job>()

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
    suspend fun run(deadlineMs: Long, id: String = Uuid.random().toString()): Boolean = coroutineScope {
        if (RustRuntime.startedSession() == null) Prefs.load()
        RustRuntime.start()
        val finished = CompletableDeferred<Boolean>()
        val observer = launch(start = CoroutineStart.UNDISPATCHED) {
            val event = RustEventHandler.events.first { event ->
                event.type == "BACKGROUND_EXECUTION_FINISHED" && JsonHelper.jsonDecode<ExecutionResult>(event.payload).id == id
            }
            finished.complete(requireNotNull(JsonHelper.jsonDecode<ExecutionResult>(event.payload).success))
        }
        try {
            withGrant(
                grant = { RustEventHandler.sendEvent(HostEvent(EventType.BACKGROUND_EXECUTION_GRANTED, JsonHelper.jsonEncode(ExecutionGrant(id, deadlineMs)))) },
                awaitCompletion = { finished.await() },
                revoke = { RustEventHandler.sendEvent(HostEvent(EventType.BACKGROUND_EXECUTION_REVOKED, JsonHelper.jsonEncode(ExecutionRevocation(id)))) },
            )
        } finally {
            withContext(NonCancellable) { observer.cancelAndJoin() }
        }
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
        if (RustRuntime.startedSession() == null) return
        scope.launch {
            try { RustEventHandler.sendEvent(HostEvent(EventType.EXECUTION_STATE_CHANGED, "{}")) }
            catch (error: Exception) { LogCat.e("Execution state report failed", error) }
        }
    }
}

internal expect suspend fun scheduleBackgroundWake(atMs: Long?)

@Serializable
private data class ExecutionResult(val id: String? = null, val success: Boolean? = null)

@Serializable
private data class ExecutionGrant(val id: String, val deadlineMs: Long)

@Serializable
private data class ExecutionRevocation(val id: String)

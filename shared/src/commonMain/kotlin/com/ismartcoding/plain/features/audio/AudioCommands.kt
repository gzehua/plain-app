package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.graphql.models.AudioItem
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.content.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

object AudioCommands {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val requests = Channel<suspend () -> Unit>(32)
    init {
        scope.launch {
            for (request in requests) {
                try { request() }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (e: Exception) { LogCat.e("Audio command: ${e.message}") }
            }
        }
    }
    private fun submit(request: suspend () -> Unit) {
        check(requests.trySend(request).isSuccess) { "Audio command capacity exceeded" }
    }
    suspend fun command(action: String, positionMs: Long = 0, speed: Float = 1f) {
        RustContentApi.mutate("audioCommand(action: $action, positionMs: $positionMs, speed: $speed) { path positionMs revision }")
    }
    fun submit(action: String, positionMs: Long = 0, speed: Float = 1f) = submit { command(action, positionMs, speed) }
    fun playTrack(audio: AudioItem) = submit {
        RustContentApi.mutate("audioPlayTrack(track: ${listOf(audio).audioInput().removePrefix("[").removeSuffix("]")}, enqueue: false) { path positionMs revision }")
    }
    fun playPath(path: String) = submit {
        RustContentApi.mutate("audioPlayPath(path: ${gql(path)}) { path positionMs revision }")
    }
}

package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.api.graphql.models.AudioItem
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.features.feeds.*
import com.ismartcoding.plain.ui.feeds.*
import com.ismartcoding.plain.features.files.*
import com.ismartcoding.plain.ui.files.*
import com.ismartcoding.plain.features.notes.*
import com.ismartcoding.plain.ui.notes.*
import com.ismartcoding.plain.features.tags.*
import com.ismartcoding.plain.ui.tags.*
import com.ismartcoding.plain.rust.request.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
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
        PlainRust.mainMutate("audioCommand(action: $action, positionMs: $positionMs, speed: $speed) { path positionMs revision }")
    }
    fun submit(action: String, positionMs: Long = 0, speed: Float = 1f) = submit { command(action, positionMs, speed) }
    fun playTrack(audio: AudioItem) = submit {
        if (!audio.mediaId.isNullOrEmpty()) {
            PlainRust.mainMutate("audioPlayTrack(mediaId: ${gql(audio.mediaId!!)}) { path positionMs revision }")
        } else {
            PlainRust.mainMutate("audioPlayPath(path: ${gql(audio.path)}) { path positionMs revision }")
        }
    }
    fun playPath(path: String) = submit {
        PlainRust.mainMutate("audioPlayPath(path: ${gql(path)}) { path positionMs revision }")
    }
}

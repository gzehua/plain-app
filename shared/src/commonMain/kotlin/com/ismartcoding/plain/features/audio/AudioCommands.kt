package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.models.AudioCommand
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
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

    suspend fun command(action: AudioCommand, positionMs: Long = 0, speed: Float = 1f) {
        RustGraphqlApi.execute(Mutation.audioCommand(action, positionMs, speed))
    }

    fun submit(
        action: AudioCommand,
        positionMs: Long = 0,
        speed: Float = 1f,
    ) = submit { command(action, positionMs, speed) }

    fun cycleMode() = submit {
        val current = AudioQueueClient.playback().mode
        val modes = MediaPlayMode.entries
        val next = modes[(modes.indexOf(current) + 1) % modes.size]
        RustGraphqlApi.execute(Mutation.updateAudioPlayMode(next))
    }

    fun playTrack(audio: AudioItem) = submit {
        if (!audio.mediaId.isNullOrEmpty()) {
            RustGraphqlApi.execute(Mutation.audioPlayTrack(audio.mediaId!!))
        } else {
            RustGraphqlApi.execute(Mutation.audioPlayPath(audio.path))
        }
    }

    fun playPath(path: String) = submit {
        RustGraphqlApi.execute(Mutation.audioPlayPath(path))
    }
}

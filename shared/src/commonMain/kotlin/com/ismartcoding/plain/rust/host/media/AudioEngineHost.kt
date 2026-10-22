package com.ismartcoding.plain.rust.host.media

import com.ismartcoding.plain.rust.event.RustEventHandler

import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.platform.media.AudioPlayer
import com.ismartcoding.plain.platform.media.audioPlayer
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
private data class AudioEngineCommand(
    val action: String,
    val playback: AudioEnginePlayback,
    val speed: Float,
    val newTrack: Boolean,
    val track: AudioItem? = null,
)

@Serializable
private data class AudioEnginePlayback(
    val revision: Long,
    val positionMs: Long,
)

object AudioEngineHost {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private var overridePlayer: AudioPlayer? = null
    private var revision = -1L
    private var reporter: Job? = null

    suspend fun install(player: AudioPlayer?): AudioPlayer? = withContext(Dispatchers.Main) {
        mutex.withLock {
            val old = overridePlayer
            overridePlayer = player
            old
        }
    }
    private fun engine(): AudioPlayer = overridePlayer ?: audioPlayer

    suspend fun start() {
        check(reporter == null) { "Audio progress reporter already started" }
        reporter = scope.launch {
            while (isActive) {
                delay(1_000)
                try {
                    val report = withContext(Dispatchers.Main) { mutex.withLock { report(engine()) } }
                    if (report.path.isNotEmpty() && report.revision >= 0) {
                        RustEventHandler.publish(com.ismartcoding.plain.app.events.HostEvent(com.ismartcoding.plain.app.events.EventType.AUDIO_PROGRESS_REPORTED, JsonHelper.jsonEncode(report)))
                    }
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (e: Exception) { LogCat.e("Audio progress: ${e.message}") }
            }
        }
    }
    suspend fun restore(value: JsonObject) {
        val restored = JsonHelper.jsonDecodeFromElement<AudioRestoreReport>(value)
        restored.error?.let { error(it) }
        val restoredRevision = requireNotNull(restored.revision)
        val position = requireNotNull(restored.positionMs)
        require(restoredRevision >= 0 && position >= 0)
        withContext(Dispatchers.Main) {
            mutex.withLock {
                if (restoredRevision <= revision) return@withLock
                revision = restoredRevision
                TempData.audioPlayPosition = position
                engine().bindRevision(restoredRevision)
            }
        }
    }

    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "audioEngineCommand" -> withContext(Dispatchers.Main) {
            mutex.withLock {
                val player = engine()
                val command = JsonHelper.jsonDecodeFromElement<AudioEngineCommand>(params)
                val requestedRevision = command.playback.revision
                val position = command.playback.positionMs
                val speed = command.speed
                require(position >= 0 && requestedRevision >= 0 && speed.isFinite() && speed > 0)
                val action = command.action
                player.bindRevision(requestedRevision)
                var loaded = false
                when (action) {
                    "PLAY", "SEEK" -> {
                        val track = requireNotNull(command.track) { "Audio track required for $action" }
                        if (action == "PLAY" && !command.newTrack && player.currentPath == track.path) {
                            player.seekTo(position)
                            player.setPlaybackSpeed(speed)
                            player.play()
                        } else if (action == "SEEK" && player.currentPath == track.path) {
                            player.seekTo(position)
                        } else { player.load(track, position, speed, requestedRevision); loaded = true }
                    }
                    "PAUSE" -> player.pause()
                    "CLEAR" -> player.clear()
                    "RESTART" -> player.restartIfPlaying()
                    "SET_SPEED" -> player.setPlaybackSpeed(speed)
                    else -> error("Unknown audio engine command: $action")
                }
                revision = requestedRevision
                val reported = report(player)
                JsonHelper.jsonEncodeToElement(AudioEngineReport(
                    reported.path, if (action == "PLAY" || action == "SEEK") position else reported.positionMs, reported.revision, loaded,
                ))
            }
        }
        else -> error("Unknown audio engine method: $method")
    }
    private fun report(player: AudioPlayer) = AudioProgressReport(player.currentPath, player.progress.coerceAtLeast(0), revision)
}

@Serializable
private data class AudioRestoreReport(val revision: Long? = null, val positionMs: Long? = null, val error: String? = null)

@Serializable
private data class AudioProgressReport(val path: String, val positionMs: Long, val revision: Long)

@Serializable
private data class AudioEngineReport(val path: String, val positionMs: Long, val revision: Long, val loaded: Boolean)

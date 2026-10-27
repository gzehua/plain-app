package com.ismartcoding.plain.rust.host.audio

import com.ismartcoding.plain.api.graphql.models.AudioPlaybackChange
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.platform.media.AudioPlayer
import com.ismartcoding.plain.platform.media.audioPlayer
import com.ismartcoding.plain.rust.host.*
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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutex = Mutex()
    private var overridePlayer: AudioPlayer? = null
    private var revision = -1L
    private var reporter: Job? = null
    private var pendingReport: Job? = null

    suspend fun install(player: AudioPlayer?): AudioPlayer? = withContext(Dispatchers.Main) {
        mutex.withLock {
            val old = overridePlayer
            overridePlayer = player
            old
        }
    }
    private fun engine(): AudioPlayer = overridePlayer ?: audioPlayer

    internal fun startProgressReporting() {
        if (reporter != null) return
        reporter = scope.launch {
            while (isActive) {
                delay(1_000)
                try {
                    val progress = mutex.withLock { sampleProgress(engine()) }
                    reportProgress(progress.path, progress.revision, progress.positionMs)?.join()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    LogCat.e("Audio progress: ${error.message}")
                }
            }
        }
    }

    internal fun stopProgressReporting() {
        reporter?.cancel()
        reporter = null
    }

    internal fun stopProgressReporting(path: String, revision: Long, positionMs: Long) {
        stopProgressReporting()
        reportProgress(path, revision, positionMs)
    }

    internal fun reportStarted(path: String, revision: Long) {
        enqueueReport(AudioHostReport.Started(path, revision))
    }

    internal fun reportCompleted(path: String, revision: Long, positionMs: Long) {
        stopProgressReporting(path, revision, positionMs)
        enqueueReport(AudioHostReport.Completed(path, revision))
    }

    internal fun reportProgress(path: String, revision: Long, positionMs: Long): Job? {
        if (path.isEmpty() || revision < 0) return null
        return enqueueReport(AudioHostReport.Progress(path, positionMs.coerceAtLeast(0), revision))
    }

    private fun enqueueReport(report: AudioHostReport): Job {
        val previous = pendingReport
        return scope.launch {
            previous?.join()
            try {
                report.sendToRust()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                LogCat.e("Audio report ${report::class.simpleName}: ${error.message}")
            }
        }.also { pendingReport = it }
    }

    internal suspend fun restore(restored: AudioPlaybackChange) {
        val restoredRevision = restored.revision
        val position = restored.positionMs
        require(restoredRevision >= 0 && position >= 0)
        withContext(Dispatchers.Main) {
            mutex.withLock {
                if (restoredRevision <= revision) return@withLock
                revision = restoredRevision
                engine().restorePlayback(restoredRevision, position)
            }
        }
    }

    internal suspend fun command(params: JsonObject): HostResult = withContext(Dispatchers.Main) {
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
            val reported = sampleProgress(player)
            hostResult(AudioEngineReport(
                reported.path, if (action == "PLAY" || action == "SEEK") position else reported.positionMs, reported.revision, loaded,
            ))
        }
    }
    private fun sampleProgress(player: AudioPlayer) = AudioHostReport.Progress(player.currentPath, player.progress.coerceAtLeast(0), revision)
}

@Serializable
private data class AudioEngineReport(val path: String, val positionMs: Long, val revision: Long, val loaded: Boolean)

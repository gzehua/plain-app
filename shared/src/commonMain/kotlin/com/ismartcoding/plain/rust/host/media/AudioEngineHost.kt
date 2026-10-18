package com.ismartcoding.plain.rust.host.media

import com.ismartcoding.plain.api.graphql.models.AudioItem
import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.*
import com.ismartcoding.plain.platform.media.AudioPlayer
import com.ismartcoding.plain.platform.media.audioPlayer
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*

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
                    val path = report.string("path")
                    if (path.isNotEmpty() && report.getValue("revision").jsonPrimitive.long >= 0) {
                        PlainRust.publish(com.ismartcoding.plain.app.events.HostEvent(com.ismartcoding.plain.app.events.EventType.AUDIO_PROGRESS_REPORTED, JsonHelper.jsonEncode(report)))
                    }
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (e: Exception) { LogCat.e("Audio progress: ${e.message}") }
            }
        }
    }
    suspend fun restore(value: JsonObject) {
        value["error"]?.jsonPrimitive?.contentOrNull?.let { error(it) }
        val restoredRevision = value.getValue("revision").jsonPrimitive.long
        val position = value.getValue("positionMs").jsonPrimitive.long
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
                val playback = params.getValue("playback").jsonObject
                val requestedRevision = playback.getValue("revision").jsonPrimitive.long
                val position = playback.getValue("positionMs").jsonPrimitive.long
                val speed = params.getValue("speed").jsonPrimitive.float
                require(position >= 0 && requestedRevision >= 0 && speed.isFinite() && speed > 0)
                val action = params.string("action")
                player.bindRevision(requestedRevision)
                var loaded = false
                when (action) {
                    "PLAY", "SEEK" -> {
                        val track = JsonHelper.jsonDecodeFromElement<AudioItem>(params.getValue("track"))
                        if (action == "PLAY" && !params.getValue("newTrack").jsonPrimitive.boolean && player.currentPath == track.path) {
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
                JsonObject(report(player) + ("loaded" to JsonPrimitive(loaded))).let { reported -> if (action == "PLAY" || action == "SEEK") JsonObject(reported + ("positionMs" to JsonPrimitive(position))) else reported }
            }
        }
        else -> error("Unknown audio engine method: $method")
    }
    private fun report(player: AudioPlayer): JsonObject = buildJsonObject {
        put("path", player.currentPath); put("positionMs", player.progress.coerceAtLeast(0)); put("revision", revision)
    }
}

package com.ismartcoding.plain.rust.host.audio

import com.ismartcoding.plain.api.graphql.models.AudioPlaybackChange
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.media.AudioPlayer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.*
import kotlin.test.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class AudioEngineHostTest {
    private class Player : AudioPlayer {
        val calls = mutableListOf<String>()
        val gate = CompletableDeferred<Unit>()
        override val isPlayingFlow = MutableStateFlow(false)
        override var currentPath = "song"
        override var progress = 0L
        var lastPositionMs = 0L
        override fun bindRevision(revision: Long) { calls += "bind:$revision" }
        override fun restorePlayback(revision: Long, positionMs: Long) {
            lastPositionMs = positionMs
            bindRevision(revision)
        }
        override suspend fun load(audio: AudioItem, positionMs: Long, speed: Float, revision: Long) {
            calls += "load"
            gate.await()
            currentPath = audio.path
            progress = positionMs
        }
        override suspend fun seekTo(positionMs: Long) {
            calls += "seek"
            gate.await()
            progress = positionMs
        }
        override suspend fun play() { calls += "play" }
        override suspend fun pause() { calls += "pause" }
        override suspend fun clear() { calls += "clear"; currentPath = ""; progress = 0L }
        override suspend fun restartIfPlaying() { calls += "restart" }
        override suspend fun setPlaybackSpeed(speed: Float) { calls += "speed:$speed" }
    }

    private fun command(newTrack: Boolean): JsonObject = JsonHelper.jsonDecode(
        """{"action":"PLAY","newTrack":$newTrack,"speed":1.5,"playback":{"path":"song","positionMs":1200,"revision":5},"track":{"path":"song","title":"Song","artist":"Artist","durationMs":2000}}""",
    )

    @Test
    fun resume_acknowledges_only_after_seek_speed_and_play_complete() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val player = Player()
        val previous = AudioEngineHost.install(player)
        try {
            val result = async { AudioEngineHost.command(command(false)) }
            runCurrent()
            assertFalse(result.isCompleted)
            assertEquals(listOf("bind:5", "seek"), player.calls)
            player.gate.complete(Unit)
            val report = JsonHelper.jsonDecode<JsonObject>(result.await().encodeReply(1)).getValue("result").jsonObject
            assertEquals(listOf("bind:5", "seek", "speed:1.5", "play"), player.calls)
            assertEquals(1200, report.getValue("positionMs").jsonPrimitive.int)
            assertEquals(5, report.getValue("revision").jsonPrimitive.int)
            assertFalse(report.getValue("loaded").jsonPrimitive.boolean)
        } finally {
            AudioEngineHost.install(previous)
            Dispatchers.resetMain()
        }
    }

    @Test
    fun loading_waits_for_the_sdk_before_reporting_the_loaded_revision() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val player = Player().apply { currentPath = "other" }
        val previous = AudioEngineHost.install(player)
        try {
            val result = async { AudioEngineHost.command(command(true)) }
            runCurrent()
            assertFalse(result.isCompleted)
            assertEquals(listOf("bind:5", "load"), player.calls)
            player.gate.complete(Unit)
            val report = JsonHelper.jsonDecode<JsonObject>(result.await().encodeReply(1)).getValue("result").jsonObject
            assertEquals("song", report.getValue("path").jsonPrimitive.content)
            assertTrue(report.getValue("loaded").jsonPrimitive.boolean)
        } finally {
            AudioEngineHost.install(previous)
            Dispatchers.resetMain()
        }
    }
    @Test
    fun restored_state_never_overwrites_a_newer_command_or_live_position() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val player = Player()
        player.gate.complete(Unit)
        val previous = AudioEngineHost.install(player)
        try {
            AudioEngineHost.command(command(true))
            player.lastPositionMs = 777
            AudioEngineHost.restore(AudioPlaybackChange(4, 0))
            assertEquals(777L, player.lastPositionMs)
            AudioEngineHost.restore(AudioPlaybackChange(6, 1234))
            assertEquals(1234L, player.lastPositionMs)
            AudioEngineHost.restore(AudioPlaybackChange(6, 0))
            assertEquals(1234L, player.lastPositionMs)
            assertEquals(listOf("bind:5", "load", "bind:6"), player.calls)
        } finally {
            AudioEngineHost.install(previous)
            Dispatchers.resetMain()
        }
    }

}

package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.features.audio.MediaPlayMode
import com.ismartcoding.plain.features.audio.*
import com.ismartcoding.plain.ui.audio.*
import com.ismartcoding.plain.api.graphql.models.AudioItem
import com.ismartcoding.plain.preferences.UserPrefs
import com.ismartcoding.plain.features.feeds.*
import com.ismartcoding.plain.ui.feeds.*
import com.ismartcoding.plain.features.files.*
import com.ismartcoding.plain.ui.files.*
import com.ismartcoding.plain.features.notes.*
import com.ismartcoding.plain.ui.notes.*
import com.ismartcoding.plain.features.tags.*
import com.ismartcoding.plain.ui.tags.*
import com.ismartcoding.plain.rust.request.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.request.*
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioEngineRustHttpTest {
    @Test
    fun commandsOwnResumeCompletionAndRejectStalePhysicalReports() = runBlocking {
        assertTrue(AudioQueueClient.queuedMediaIds().isEmpty())
        assertTrue(AudioQueueClient.playback().path.isNullOrEmpty())
        val prefix = "audio-engine-${UUID.randomUUID()}"
        fun track(index: Int) = AudioItem(title = "Track $index", path = "$prefix/$index", artist = "Synthetic", durationMs = 117123, mediaId = "$prefix-$index")
        val oldProvider = AudioLibraryHost.install(object : AudioLibraryProvider {
            override suspend fun resolve(ids: List<String>, paths: List<String>): List<AudioMedia> = (0 until 2).map(::track).filter { it.mediaId in ids || it.path in paths }.map { AudioMedia(it, it.mediaId) }
        })
        val engine = AudioTestPlayer()
        val oldEngine = AudioEngineHost.install(engine)
        val oldMode = UserPrefs.audioPlayMode.value
        try {
            AudioQueueClient.enqueue(listOf(track(0),track(1)))
            val played = com.ismartcoding.plain.rust.PlainRust.mainMutate("audioPlayTrack(mediaId: ${JsonPrimitive(track(0).mediaId)}) {path revision positionMs}").getValue("audioPlayTrack").jsonObject
            assertEquals(track(0).path, played.getValue("path").jsonPrimitive.content)
            val initialRevision = played.getValue("revision").jsonPrimitive.long
            AudioQueueClient.onStarted(track(0), initialRevision)
            AudioQueueClient.onStarted(track(0), initialRevision)
            assertEquals(1, AudioHistoryClient.recentPageFiltered(prefix,20,0).single().playCount)
            AudioCommands.command("SEEK", 3_000_000_123)
            com.ismartcoding.plain.rust.PlainRust.publish(com.ismartcoding.plain.app.events.HostEvent(
                com.ismartcoding.plain.app.events.EventType.AUDIO_PROGRESS_REPORTED,
                """{"path":${JsonPrimitive(track(0).path)},"revision":$initialRevision,"positionMs":0}""",
            ))
            assertEquals(3_000_000_123, AudioQueueClient.playback().positionMs)
            AudioCommands.command("PAUSE")
            AudioCommands.command("PLAY")
            assertEquals(3_000_000_123, engine.progress)
            assertEquals(1, engine.loads)
            UserPrefs.audioPlayMode.value = MediaPlayMode.REPEAT_ONE
            AudioCommands.command("COMPLETED")
            assertEquals(track(0).path, engine.currentPath)
            assertEquals(0, engine.progress)
            assertEquals(2, engine.loads)
            AudioCommands.command("NEXT")
            assertEquals(track(1).path, engine.currentPath)
            engine.fail = true
            var failed = false
            try { AudioCommands.command("PAUSE") } catch (_: Exception) { failed = true }
            assertTrue("Native player failures must propagate", failed)
            engine.fail = false
        } finally {
            try {
                engine.fail = false
                AudioCommands.command("CLEAR")
                AudioQueueClient.clearQueue()
                UserPrefs.audioPlayMode.value = oldMode
            } finally {
                AudioEngineHost.install(oldEngine)
                AudioLibraryHost.install(oldProvider)
            }
        }
    }
}

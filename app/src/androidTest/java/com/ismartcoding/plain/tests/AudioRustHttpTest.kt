package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.features.audio.*
import com.ismartcoding.plain.ui.audio.*
import com.ismartcoding.plain.api.graphql.models.AudioItem
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioRustHttpTest {
    @Test
    fun playlistsQueueHistoryAndLargeLibraryUseRustWithNativeHostCallbacks() = runBlocking {
        assertTrue(AudioQueueClient.queuedMediaIds().isEmpty())
        assertTrue(AudioQueueClient.playback().path.isNullOrEmpty())
        val prefix = "audio-rust-${UUID.randomUUID()}"
        fun track(index: Int): AudioItem = AudioItem(title = "Track $index", path = "$prefix/$index", artist = "Synthetic $prefix", durationMs = 117123, mediaId = "$prefix-$index")
        val previousProvider = AudioLibraryHost.install(object : AudioLibraryProvider {
            override suspend fun resolve(ids: List<String>, paths: List<String>): List<AudioMedia> = (0 until 10_000).map(::track).filter { it.mediaId in ids || it.path in paths }.map { AudioMedia(it, it.mediaId) }
        })
        var playlistId = ""
        try {
            val list = AudioPlaylistsClient.createPlaylist(prefix)
            playlistId = list.id
            assertEquals(2, AudioPlaylistsClient.addPlaylistItems(list.id,listOf(track(1),track(2),track(1))))
            val items = AudioPlaylistsClient.playlistItemsPage(list.id,0,20)
            assertEquals(listOf(track(1).mediaId, track(2).mediaId), items.map { it.mediaId })
            assertEquals(listOf(117123L,117123L), items.map { it.durationMs })
            AudioQueueClient.add("playlist_id:${list.id}")
            assertEquals(listOf(track(1).mediaId,track(2).mediaId), AudioQueueClient.queuePage(0,20).map { it.mediaId })
            AudioQueueClient.setCurrent(track(1).mediaId)
            assertEquals(track(2).mediaId, checkNotNull(AudioQueueClient.resolveNext(true,false)).mediaId)
            assertTrue(AudioHistoryClient.recentPageFiltered(prefix,20,0).isEmpty())
            AudioQueueClient.clearQueue()
            AudioQueueClient.enqueue((0 until 10_000).map(::track))
            assertEquals(10000, AudioQueueClient.queueTotal())
            assertEquals(track(9009).mediaId, AudioQueueClient.queuePage(8990,20).last().mediaId)
            AudioQueueClient.setCurrent(track(9000).mediaId)
            assertEquals(track(9001).mediaId, checkNotNull(AudioQueueClient.resolveNext(true,false)).mediaId)
            AudioQueueClient.reorderQueued(listOf(track(2).mediaId!!,track(1).mediaId!!,track(0).mediaId!!))
            assertEquals(listOf(2,1,0,3,4).map { track(it).mediaId }, AudioQueueClient.queuePage(0,5).map { it.mediaId })
            AudioQueueClient.removeQueued(track(1).mediaId!!)
            assertEquals(2,AudioPlaylistsClient.playlistItemCount(list.id))
            AudioPlaylistsClient.removePlaylistItem(list.id, track(1).mediaId!!)
            assertEquals(1,AudioPlaylistsClient.playlistItemCount(list.id))
        } finally {
            try {
                AudioQueueClient.clearQueue()
                if (playlistId.isNotEmpty()) AudioPlaylistsClient.deletePlaylist(playlistId)
            } finally { AudioLibraryHost.install(previousProvider) }
        }
    }
}

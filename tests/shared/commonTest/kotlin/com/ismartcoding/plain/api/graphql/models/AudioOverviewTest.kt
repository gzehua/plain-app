package com.ismartcoding.plain.models

import com.ismartcoding.plain.lib.JsonHelper
import kotlin.test.*

class AudioOverviewTest {
    @Test
    fun main_summary_decodes_full_media_rows_and_large_play_counts() {
        val overview = JsonHelper.jsonDecode<AudioOverview>("""{
            "artists":[{"name":"Artist","itemCount":1205,"playCount":3000000123,"samplePath":"/music/song.mp3"}],
            "recentItems":[{"id":"track","title":"Song","artist":"Artist","path":"/music/song.mp3","durationMs":1234,"size":2000,"bucketId":"music","albumId":"album","albumFileId":"cover","createdAt":"2026-01-01T00:00:00Z","updatedAt":"2026-01-01T00:00:00Z","isFavorite":true}]
        }""")
        assertEquals(1205, overview.artists.single().itemCount)
        assertEquals(3000000123L, overview.artists.single().playCount)
        assertEquals("track", overview.recentItems.single().id)
        assertEquals("album", overview.recentItems.single().albumId)
        assertTrue(overview.recentItems.single().isFavorite)
    }
}

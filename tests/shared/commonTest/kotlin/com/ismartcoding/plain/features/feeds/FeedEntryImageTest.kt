package com.ismartcoding.plain.features.feeds

import com.ismartcoding.plain.models.FeedEntry
import kotlin.test.Test
import kotlin.test.assertEquals

class FeedEntryImageTest {
    private fun entryWithImage(image: String) = FeedEntry(image = image)

    @Test
    fun keeps_remote_artwork_as_the_link_it_is() {
        assertEquals(
            "https://cdn.example.com/a.jpg",
            entryWithImage("https://cdn.example.com/a.jpg").displayImage,
        )
        assertEquals(
            "http://cdn.example.com/a.jpg",
            entryWithImage("http://cdn.example.com/a.jpg").displayImage,
        )
    }

    @Test
    fun keeps_core_file_references_untouched() {
        assertEquals("fid:abc123.jpg", entryWithImage("fid:abc123.jpg").displayImage)
        assertEquals("app://photos/a.jpg", entryWithImage("app://photos/a.jpg").displayImage)
    }

    @Test
    fun keeps_a_plain_path_untouched() {
        assertEquals("/data/a.jpg", entryWithImage("/data/a.jpg").displayImage)
    }

    @Test
    fun an_entry_without_artwork_stays_empty() {
        assertEquals("", entryWithImage("").displayImage)
    }
}
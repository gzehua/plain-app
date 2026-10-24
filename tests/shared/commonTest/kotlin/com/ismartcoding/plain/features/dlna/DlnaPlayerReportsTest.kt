package com.ismartcoding.plain.features.dlna

import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HostEvent
import com.ismartcoding.plain.app.events.HostEventData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.*

class DlnaPlayerReportsTest {
    private fun HostEvent.payload() = Json.parseToJsonElement((data as HostEventData.Text).value).jsonObject

    @Test
    fun playerFactsPreserveMediaIdentityStateNamesAndExactMilliseconds() {
        val uri = "https://example.test/owned?name=\"αβ\""
        val position = DlnaPlayerReports.position(uri, Long.MAX_VALUE, Long.MAX_VALUE - 1)
        assertEquals(EventType.DLNA_POSITION_REPORTED, position.type)
        assertEquals(uri, position.payload().getValue("mediaUri").jsonPrimitive.content)
        assertEquals(Long.MAX_VALUE, position.payload().getValue("positionMs").jsonPrimitive.long)
        assertEquals(Long.MAX_VALUE - 1, position.payload().getValue("durationMs").jsonPrimitive.long)
        val playback = DlnaPlayerReports.playback(uri, DlnaPlaybackState.PAUSED)
        assertEquals(EventType.DLNA_PLAYBACK_REPORTED, playback.type)
        assertEquals("PausedPlayback", playback.payload().getValue("state").jsonPrimitive.content)
        val seek = DlnaPlayerReports.seekConsumed(uri, 5_000_000_001)
        assertEquals(EventType.DLNA_SEEK_CONSUMED_REPORTED, seek.type)
        assertEquals(5_000_000_001, seek.payload().getValue("positionMs").jsonPrimitive.long)
        val stopped = DlnaPlayerReports.stopped(uri)
        assertEquals(EventType.DLNA_STOPPED_REPORTED, stopped.type)
        assertEquals(setOf("mediaUri"), stopped.payload().keys)
        assertEquals(uri, stopped.payload().getValue("mediaUri").jsonPrimitive.content)
    }
}

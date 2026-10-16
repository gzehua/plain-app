package com.ismartcoding.plain.features.dlna

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.*

class ReceiverCommandTest {
    @Test
    fun typedInputPreservesPlaybackNamesLongValuesAndFalseFlags() {
        val playback = ReceiverCommand.Playback(DlnaPlaybackState.PAUSED).input()
        assertEquals("PLAYBACK", playback.getValue("action").jsonPrimitive.content)
        assertEquals("PAUSED_PLAYBACK", playback.getValue("state").jsonPrimitive.content)
        val position = ReceiverCommand.Position(Long.MAX_VALUE, Long.MAX_VALUE - 1).input()
        assertEquals(Long.MAX_VALUE, position.getValue("positionMs").jsonPrimitive.long)
        assertEquals(Long.MAX_VALUE - 1, position.getValue("durationMs").jsonPrimitive.long)
        val rule = ReceiverCommand.RemoveRule("example", false).input()
        assertEquals("REMOVE_RULE", rule.getValue("action").jsonPrimitive.content)
        assertEquals(false, rule.getValue("allowed").jsonPrimitive.boolean)
        assertEquals("SEEK_CONSUMED", ReceiverCommand.SeekConsumed.input().getValue("action").jsonPrimitive.content)
    }
}

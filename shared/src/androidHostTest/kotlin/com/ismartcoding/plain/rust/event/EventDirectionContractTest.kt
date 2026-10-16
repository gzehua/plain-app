package com.ismartcoding.plain.rust.event

import com.ismartcoding.plain.rust.request.*

import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HEvent
import com.ismartcoding.plain.app.events.HostEvent
import com.ismartcoding.plain.app.events.HostEventData
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class EventDirectionContractTest {
    @Test
    fun wirePacketsAndCapabilitiesUseNamesWithoutNumericAliases() {
        val packet = JsonHelper.jsonEncodeToElement(HostEventPacket(EventType.SCREEN_MIRROR_VIDEO_CODEC.name, "{}"))
        val kind = packet.jsonObject.getValue("type").jsonPrimitive
        assertTrue(kind.isString)
        assertEquals("SCREEN_MIRROR_VIDEO_CODEC", kind.content)
        val greeting = JsonHelper.jsonDecode<RustEvent>("""{"type":"CONTENT_CHANGED","payload":"{}","reconcile":true}""")
        assertEquals(EventType.CONTENT_CHANGED.name, greeting.type)
        val capabilities = HostEventCapabilities(setOf("SCREEN_MIRROR_VIDEO_CODEC"), setOf("SCREEN_MIRROR_VIDEO"))
        assertTrue(capabilities.accepts(EventType.SCREEN_MIRROR_VIDEO_CODEC, HostEventData.Text("{}")))
        assertTrue(capabilities.accepts(EventType.SCREEN_MIRROR_VIDEO, HostEventData.Binary(byteArrayOf(0, 1))))
        assertFalse(capabilities.accepts(EventType.SCREEN_MIRROR_VIDEO, HostEventData.Text("{}")))
        assertFalse(capabilities.accepts(EventType.PAIRING_REQUEST_RECEIVED, HostEventData.Text("{}")))
        assertTrue(EventType.entries.all { it.name.matches(Regex("[A-Z_]+")) })
    }

    @Test
    fun httpEventsHaveAnExplicitLocalOriginAndProjectionsCannotPublish() {
        val root = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
            .first { File(it, "shared/src/commonMain").isDirectory }
        val sources = File(root, "shared/src/commonMain/kotlin/com/ismartcoding/plain")
        val httpEvents = File(sources, "app/events/HttpApiEvents.kt").readText()
        val declarations = Regex("(?:data )?class (\\w+)[^\\n]*: (\\w+)\\(\\)")
            .findAll(httpEvents).toList()
        assertTrue(declarations.isNotEmpty())
        declarations.forEach { declaration ->
            assertTrue(declaration.value, declaration.groupValues[1].startsWith("H"))
            assertTrue(declaration.value, declaration.groupValues[2] == HEvent::class.java.simpleName)
        }
        assertTrue(!HEvent::class.java.isAssignableFrom(HostEvent::class.java))
        for (path in listOf("rust/event/RustEventProjection.kt", "features/nearby/PairingProjection.kt",
            "features/nearby/RustNearbyDevices.kt", "features/nearby/RustMdnsRuntime.kt",
            "features/chat/channel/ChannelSystemMessageReceiver.kt", "features/images/search/RustImageModels.kt",
            "features/dlna/DlnaRendererState.kt", "features/pomodoro/PomodoroHost.kt")) {
            val source = File(sources, path).readText()
            assertTrue("$path must only project HTTP state locally", !source.contains("HostEvent"))
        }
    }
}

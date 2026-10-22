package com.ismartcoding.plain.rust

import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HChatMessageNotificationEvent
import com.ismartcoding.plain.app.events.HEvent
import com.ismartcoding.plain.app.events.HostEvent
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
    fun wirePacketsAndHostEventTypesUseNamesWithoutNumericAliases() {
        val packet = JsonHelper.jsonEncodeToElement(mapOf("type" to EventType.SCREEN_MIRROR_VIDEO_CODEC.name, "payload" to "{}"))
        val kind = packet.jsonObject.getValue("type").jsonPrimitive
        assertTrue(kind.isString)
        assertEquals("SCREEN_MIRROR_VIDEO_CODEC", kind.content)
        assertEquals("{}", packet.jsonObject.getValue("payload").jsonPrimitive.content)
        val greeting = JsonHelper.jsonDecode<RustEvent>("""{"type":"CONTENT_CHANGED","payload":"{}","reconcile":true}""")
        assertEquals(EventType.CONTENT_CHANGED.name, greeting.type)
        assertTrue(greeting.reconcile)
        val types = JsonHelper.jsonDecode<HostEventTypes>(
            """{"textTypes":["SCREEN_MIRROR_VIDEO_CODEC"],"binaryTypes":["SCREEN_MIRROR_VIDEO"]}""",
        )
        assertTrue(EventType.SCREEN_MIRROR_VIDEO_CODEC.name in types.textTypes)
        assertTrue(EventType.SCREEN_MIRROR_VIDEO.name in types.binaryTypes)
        assertFalse(EventType.SCREEN_MIRROR_VIDEO.name in types.textTypes)
        assertFalse(EventType.PAIRING_REQUEST_RECEIVED.name in types.textTypes)
        assertTrue(EventType.entries.all { it.name.matches(Regex("[A-Z_]+")) })
    }

    @Test
    fun httpEventsHaveAnExplicitLocalOriginAndHandlersCannotSendHostEvents() {
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
        assertTrue(HEvent::class.java.isAssignableFrom(HChatMessageNotificationEvent::class.java))
        val handler = File(sources, "rust/RustEventHandler.kt").readText()
        val stateUpdates = handler.substringAfter("    internal suspend fun handleEvent(", missingDelimiterValue = "")
            .substringBefore("\n}\n")
        assertTrue(stateUpdates.isNotBlank())
        assertFalse(stateUpdates.contains("HostEvent"))
        assertFalse(stateUpdates.contains("RustNativeApi.send"))
        for (path in listOf("features/chat/ChatMessageReceiver.kt", "features/nearby/PairingProjection.kt",
            "features/nearby/RustNearbyDevices.kt", "features/nearby/RustMdnsRuntime.kt",
            "features/chat/channel/ChannelSystemMessageReceiver.kt", "features/images/search/RustImageModels.kt",
            "features/dlna/DlnaRendererState.kt", "ui/pomodoro/PomodoroProjection.kt")) {
            val source = File(sources, path).readText()
            assertTrue("$path must only update HTTP state locally", !source.contains("HostEvent"))
        }
    }
}

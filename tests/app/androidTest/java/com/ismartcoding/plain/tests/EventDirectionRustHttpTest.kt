package com.ismartcoding.plain.tests

import com.ismartcoding.plain.rust.sendEventToRust

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HostEvent
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventDirectionRustHttpTest {
    @Test
    fun rustStateEventsCannotBePublishedThroughThePlatformChannel() = runBlocking {
        for (type in listOf(EventType.PAIRING_REQUEST_RECEIVED, EventType.PAIRING_CANCELED,
            EventType.NEARBY_DEVICE_FOUND, EventType.NEARBY_DISCOVERY_STARTED,
            EventType.MESSAGE_CREATED, EventType.DOWNLOAD_PROGRESS, EventType.CONTENT_CHANGED,
            EventType.DEVICE_NAME_UPDATED, EventType.POMODORO_SETTINGS_UPDATE, EventType.NOTIFICATION_REFRESHED)) {
            val error = runCatching { sendEventToRust(HostEvent(type, "{}")) }.exceptionOrNull()
            assertTrue("$type must be rejected by Rust", error is IllegalArgumentException)
        }
    }
}

package com.ismartcoding.plain.features.notifications

import com.ismartcoding.plain.rust.sendEventToRust

import com.ismartcoding.plain.models.Notification as AppNotification
import com.ismartcoding.plain.events.HostEvent
import com.ismartcoding.plain.events.EventType
import com.ismartcoding.plain.lib.JsonHelper

internal object RustNotificationEvents {
    suspend fun publish(type: EventType, notification: AppNotification) {
        sendEventToRust(HostEvent(EventType.NOTIFICATION_REPORTED, JsonHelper.jsonEncode(NotificationEventFacts(type.name, notification))))
    }
}

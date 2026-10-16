package com.ismartcoding.plain.features.notification

import com.ismartcoding.plain.graphql.models.Notification as AppNotification
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.events.HostEvent
import com.ismartcoding.plain.events.EventType
import com.ismartcoding.plain.lib.JsonHelper

internal object RustNotificationEvents {
    suspend fun publish(type: EventType, notification: AppNotification) {
        PlainRust.publish(HostEvent(EventType.NOTIFICATION_REPORTED, JsonHelper.jsonEncode(NotificationEventFacts(type.name, notification))))
    }
}

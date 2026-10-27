package com.ismartcoding.plain.features.notifications

import com.ismartcoding.plain.rust.native.RustHostReports

import com.ismartcoding.plain.models.Notification as AppNotification
import com.ismartcoding.plain.lib.JsonHelper

internal object RustNotificationEvents {
    suspend fun publish(type: NotificationChange, notification: AppNotification) {
        RustHostReports.notification(JsonHelper.jsonEncode(NotificationEventFacts(type, notification)))
    }
}

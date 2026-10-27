package com.ismartcoding.plain.features.notifications

import com.ismartcoding.plain.models.Notification as AppNotification
import kotlinx.serialization.Serializable

@Serializable
internal data class NotificationEventFacts(val type: NotificationChange, val notification: AppNotification)

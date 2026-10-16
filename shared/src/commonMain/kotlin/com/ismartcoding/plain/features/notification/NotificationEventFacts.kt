package com.ismartcoding.plain.features.notification

import com.ismartcoding.plain.graphql.models.Notification as AppNotification
import kotlinx.serialization.Serializable

@Serializable
internal data class NotificationEventFacts(val type: String, val notification: AppNotification)

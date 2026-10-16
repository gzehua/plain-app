package com.ismartcoding.plain.features.notifications

import com.ismartcoding.plain.api.graphql.models.Notification as AppNotification
import kotlinx.serialization.Serializable

@Serializable
internal data class NotificationEventFacts(val type: String, val notification: AppNotification)

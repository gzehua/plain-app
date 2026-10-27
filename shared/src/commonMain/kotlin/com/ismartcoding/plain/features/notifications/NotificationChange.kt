package com.ismartcoding.plain.features.notifications

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class NotificationChange {
    @SerialName("NOTIFICATION_CREATED") CREATED,
    @SerialName("NOTIFICATION_UPDATED") UPDATED,
    @SerialName("NOTIFICATION_DELETED") DELETED,
}

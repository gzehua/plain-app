package com.ismartcoding.plain.features.notifications

import kotlinx.serialization.Serializable

@Serializable
data class NotificationFilterData(
    val mode: NotificationFilterMode,
    val apps: Set<String>,
)

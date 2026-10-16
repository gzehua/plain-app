package com.ismartcoding.plain.features.notifications

import kotlinx.serialization.Serializable

@Serializable
data class NotificationFilterData(
    val mode: String,
    val apps: Set<String>
)

package com.ismartcoding.plain.features.notifications

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class NotificationFilterMode {
    @SerialName("allowlist")
    ALLOWLIST,
    @SerialName("blacklist")
    BLACKLIST,
}

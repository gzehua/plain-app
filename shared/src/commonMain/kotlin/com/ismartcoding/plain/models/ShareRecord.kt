package com.ismartcoding.plain.models

import com.ismartcoding.plain.lib.TimeHelper
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Required

@Serializable
data class ShareRecord(
    var id: String,
    @Required var name: String = "",
    @Required var password: String = "",
    @Required var urlToken: String = "",
    @Required var readOnly: Boolean = true,
    @Required var expiresAt: Instant? = null,
    @Required var createdAt: Instant = TimeHelper.now(),
    @Required var updatedAt: Instant = TimeHelper.now(),
    var roots: List<Root>,
) {
    val isExpired: Boolean get() = expiresAt?.let { it <= TimeHelper.now() } ?: false
    val isActive: Boolean get() = !isExpired
}

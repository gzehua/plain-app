package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.lib.TimeHelper
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ShareRecord(
    var id: String,
    @kotlinx.serialization.Required var name: String = "",
    @kotlinx.serialization.Required var password: String = "",
    @kotlinx.serialization.Required var urlToken: String = "",
    @kotlinx.serialization.Required var readOnly: Boolean = true,
    @kotlinx.serialization.Required var expiresAt: Instant? = null,
    @kotlinx.serialization.Required var createdAt: Instant = TimeHelper.now(),
    @kotlinx.serialization.Required var updatedAt: Instant = TimeHelper.now(),
    var roots: List<Root>,
) {
    val isExpired: Boolean get() = expiresAt?.let { it <= TimeHelper.now() } ?: false
    val isActive: Boolean get() = !isExpired
}

package com.ismartcoding.plain.features.shares

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SharedInfoDto(
    val name: String = "",
    val readOnly: Boolean = true,
    val requiresPassword: Boolean = false,
    val expiresAt: Instant? = null,
    val urlToken: String = "",
    val entries: List<SharedFileDto> = emptyList(),
)

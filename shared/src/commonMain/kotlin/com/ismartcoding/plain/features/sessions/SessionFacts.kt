package com.ismartcoding.plain.features.sessions

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
internal data class SessionFacts(
    val clientId: String,
    @SerialName("clientName") val name: String,
    val type: String,
    val token: String,
    val clientIp: String,
    val osName: String,
    val osVersion: String,
    val browserName: String,
    val browserVersion: String,
    val createdAt: String,
    val updatedAt: String,
    val lastSeenAt: String?,
)

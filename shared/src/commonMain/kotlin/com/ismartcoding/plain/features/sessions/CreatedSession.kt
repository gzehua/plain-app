package com.ismartcoding.plain.features.sessions

import kotlinx.serialization.Serializable

@Serializable
data class CreatedSession(val clientId: String, val token: String)

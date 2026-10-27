package com.ismartcoding.plain.features.sessions


import kotlinx.serialization.Serializable

@Serializable
data class WebLoginRequestFacts(val clientId: String, val request: AuthRequest, val requestId: String, val clientIp: String)

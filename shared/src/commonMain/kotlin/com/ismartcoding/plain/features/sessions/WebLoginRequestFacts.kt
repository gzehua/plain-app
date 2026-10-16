package com.ismartcoding.plain.features.sessions

import com.ismartcoding.plain.rust.http.request

import kotlinx.serialization.Serializable

@Serializable
internal data class WebLoginRequestFacts(val clientId: String, val request: com.ismartcoding.plain.features.sessions.AuthRequest, val requestId: String, val clientIp: String)

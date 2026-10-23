package com.ismartcoding.plain.features.sessions

import com.ismartcoding.plain.network.request

import kotlinx.serialization.Serializable

@Serializable
internal data class WebLoginRequestFacts(val clientId: String, val request: AuthRequest, val requestId: String, val clientIp: String)

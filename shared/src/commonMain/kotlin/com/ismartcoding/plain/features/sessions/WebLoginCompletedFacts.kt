package com.ismartcoding.plain.features.sessions

import com.ismartcoding.plain.network.request

import kotlinx.serialization.Serializable

@Serializable
internal data class WebLoginCompletedFacts(val request: AuthRequest, val chatPaired: Boolean, val clientIp: String)

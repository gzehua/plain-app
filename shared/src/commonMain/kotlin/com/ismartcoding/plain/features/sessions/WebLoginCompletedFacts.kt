package com.ismartcoding.plain.features.sessions


import kotlinx.serialization.Serializable

@Serializable
internal data class WebLoginCompletedFacts(val request: AuthRequest, val chatPaired: Boolean, val clientIp: String)

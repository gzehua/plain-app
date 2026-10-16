package com.ismartcoding.plain.platform

import kotlinx.serialization.Serializable

@Serializable
internal data class HttpServerFailure(val generation: Long, val message: String)

@Serializable
internal data class HttpServerStarted(val generation: Long, val httpPort: Int, val httpsPort: Int)

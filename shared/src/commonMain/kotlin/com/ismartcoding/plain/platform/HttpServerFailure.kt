package com.ismartcoding.plain.platform

import kotlinx.serialization.Serializable

@Serializable
internal data class HttpServerFailure(val generation: Long, val message: String)

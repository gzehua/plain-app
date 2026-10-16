package com.ismartcoding.plain.platform.network

import kotlinx.serialization.Serializable

@Serializable
data class AwareConfig(val pmk: String?, val isClient: Boolean, val localPort: Int)

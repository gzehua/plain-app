package com.ismartcoding.plain.platform

import kotlinx.serialization.Serializable

@Serializable
internal data class CastAddressFacts(val ip: String, val defaultDeviceName: String)

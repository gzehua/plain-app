package com.ismartcoding.plain.rust.host.network

import kotlinx.serialization.Serializable

@Serializable
internal data class MulticastPermissionRequest(val lease: String, val acquire: Boolean)

package com.ismartcoding.plain.features.nearby

import kotlinx.serialization.Serializable

@Serializable
internal data class MulticastPermissionRequest(val lease: String, val acquire: Boolean)

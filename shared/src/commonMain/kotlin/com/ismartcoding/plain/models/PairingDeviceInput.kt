package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class PairingDeviceInput(
    val id: String? = null,
    val host: String? = null,
    val mode: String? = null,
    val password: String? = null,
    val generation: String? = null,
)

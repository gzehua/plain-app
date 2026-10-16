package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
data class LocationFacts(
    val latitude: Double,
    val longitude: Double,
)

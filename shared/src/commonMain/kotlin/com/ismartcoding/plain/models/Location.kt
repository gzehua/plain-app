package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    var latitude: Double,
    var longitude: Double,
)

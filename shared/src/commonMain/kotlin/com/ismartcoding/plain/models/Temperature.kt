package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class Temperature(
    var label: String,
    var celsius: Double,
)

package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class Temperature(
    var label: String,
    var celsius: Double,
)

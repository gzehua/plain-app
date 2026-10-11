package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    var latitude: Double,
    var longitude: Double,
)

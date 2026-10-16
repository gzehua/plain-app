package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    var latitude: Double,
    var longitude: Double,
)

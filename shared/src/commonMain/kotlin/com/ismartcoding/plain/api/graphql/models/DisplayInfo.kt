package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class DisplayInfo(
    var width: Int,
    var height: Int,
    var density: Double,
)

package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class DisplayInfo(
    var width: Int,
    var height: Int,
    var density: Double,
)

package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class DisplayInfo(
    var width: Int,
    var height: Int,
    var density: Double,
)

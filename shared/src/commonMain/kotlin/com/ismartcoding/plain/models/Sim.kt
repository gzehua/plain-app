package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class Sim(
    var id: String,
    var label: String,
    var number: String,
    var subscriptionId: Int = -1,
)

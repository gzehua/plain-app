package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
internal data class SimFacts(
    val id: String,
    val label: String,
    val number: String,
    val subscriptionId: Int,
)

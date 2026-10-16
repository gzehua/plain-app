package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
internal data class ContactDetailFacts(
    val value: String,
    val type: Int,
    val label: String,
)

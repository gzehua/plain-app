package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
internal data class ContactSourceFacts(
    val name: String,
    val type: String,
)

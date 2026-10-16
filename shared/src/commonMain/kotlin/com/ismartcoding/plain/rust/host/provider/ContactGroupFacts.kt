package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
internal data class ContactGroupFacts(
    val id: String,
    val name: String,
)

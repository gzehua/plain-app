package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
internal data class ContactOrganizationFacts(
    val company: String,
    val title: String,
)

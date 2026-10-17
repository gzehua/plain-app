package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
internal data class ContactPageFacts(
    val rows: List<ContactFacts> = emptyList(),
    val details: List<ContactDataFacts> = emptyList(),
    val groups: List<ContactGroupFacts> = emptyList(),
)

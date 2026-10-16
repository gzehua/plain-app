package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
internal data class ContactPhoneFacts(
    val value: String,
    val type: Int,
    val label: String,
    val normalizedNumber: String,
)

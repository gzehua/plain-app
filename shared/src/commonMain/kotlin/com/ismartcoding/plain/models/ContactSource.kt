package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class ContactSource(
    var name: String,
    var type: String,
)

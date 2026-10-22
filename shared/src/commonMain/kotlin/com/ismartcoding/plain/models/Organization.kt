package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class Organization(
    var company: String,
    var title: String,
)

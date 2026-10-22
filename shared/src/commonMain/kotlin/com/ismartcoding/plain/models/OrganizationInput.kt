package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class OrganizationInput(
    var company: String,
    var title: String,
)

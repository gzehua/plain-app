package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class OrganizationInput(
    var company: String,
    var title: String,
)

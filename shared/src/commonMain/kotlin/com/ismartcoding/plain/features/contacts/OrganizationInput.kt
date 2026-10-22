package com.ismartcoding.plain.features.contacts

import kotlinx.serialization.Serializable

@Serializable
data class OrganizationInput(var company: String, var title: String)

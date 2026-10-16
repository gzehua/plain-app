package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ContactGroup(
    var id: String,
    var name: String,
    var contactCount: Int = 0,
)

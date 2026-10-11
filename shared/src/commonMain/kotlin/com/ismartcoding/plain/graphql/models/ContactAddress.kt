package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.PostalType
import kotlinx.serialization.Serializable

@Serializable
data class ContactAddress(
    var value: String,
    var type: PostalType,
    var label: String,
)

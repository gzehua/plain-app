package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.EmailType
import kotlinx.serialization.Serializable

@Serializable
data class ContactEmail(
    var value: String,
    var type: EmailType,
    var label: String,
)

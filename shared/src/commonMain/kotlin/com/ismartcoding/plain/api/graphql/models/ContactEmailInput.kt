package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.features.telephony.EmailType
import kotlinx.serialization.Serializable

@Serializable
data class ContactEmailInput(
    var value: String,
    var type: EmailType,
    var label: String,
)

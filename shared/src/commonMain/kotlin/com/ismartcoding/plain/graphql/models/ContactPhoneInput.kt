package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.PhoneType
import kotlinx.serialization.Serializable

@Serializable
data class ContactPhoneInput(
    var value: String,
    var type: PhoneType,
    var label: String,
)

package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.PhoneType
import kotlinx.serialization.Serializable

@Serializable
data class ContactPhoneNumber(
    var value: String,
    var type: PhoneType,
    var label: String,
    var normalizedNumber: String,
    var typeCode: Int = 0,
)

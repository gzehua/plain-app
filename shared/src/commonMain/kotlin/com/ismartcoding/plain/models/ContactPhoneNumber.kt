package com.ismartcoding.plain.models

import com.ismartcoding.plain.features.telephony.PhoneType
import kotlinx.serialization.Serializable

@Serializable
data class ContactPhoneNumber(
    var value: String,
    var type: PhoneType,
    var label: String,
    var normalizedNumber: String,
    var typeCode: Int = 0,
)

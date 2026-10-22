package com.ismartcoding.plain.models

import com.ismartcoding.plain.features.telephony.PhoneType
import kotlinx.serialization.Serializable

@Serializable
data class ContactPhoneInput(
    var value: String,
    var type: PhoneType,
    var label: String,
)

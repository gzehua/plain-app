package com.ismartcoding.plain.models

import com.ismartcoding.plain.features.telephony.EmailType
import kotlinx.serialization.Serializable

@Serializable
data class ContactEmailInput(
    var value: String,
    var type: EmailType,
    var label: String,
)

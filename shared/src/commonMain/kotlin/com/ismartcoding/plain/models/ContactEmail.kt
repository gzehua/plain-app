package com.ismartcoding.plain.models

import com.ismartcoding.plain.features.telephony.EmailType
import kotlinx.serialization.Serializable

@Serializable
data class ContactEmail(
    var value: String,
    var type: EmailType,
    var label: String,
    var typeCode: Int = 0,
)

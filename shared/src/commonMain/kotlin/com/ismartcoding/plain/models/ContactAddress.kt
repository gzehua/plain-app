package com.ismartcoding.plain.models

import com.ismartcoding.plain.features.telephony.PostalType
import kotlinx.serialization.Serializable

@Serializable
data class ContactAddress(
    var value: String,
    var type: PostalType,
    var label: String,
    var typeCode: Int = 0,
)

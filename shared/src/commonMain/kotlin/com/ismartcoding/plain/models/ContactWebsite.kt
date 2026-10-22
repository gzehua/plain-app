package com.ismartcoding.plain.models

import com.ismartcoding.plain.features.telephony.WebsiteType
import kotlinx.serialization.Serializable

@Serializable
data class ContactWebsite(
    var value: String,
    var type: WebsiteType,
    var label: String,
    var typeCode: Int = 0,
)

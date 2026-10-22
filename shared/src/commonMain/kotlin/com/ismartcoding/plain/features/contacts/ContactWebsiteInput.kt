package com.ismartcoding.plain.features.contacts

import com.ismartcoding.plain.features.telephony.WebsiteType
import kotlinx.serialization.Serializable

@Serializable
data class ContactWebsiteInput(var value: String, var type: WebsiteType, var label: String)

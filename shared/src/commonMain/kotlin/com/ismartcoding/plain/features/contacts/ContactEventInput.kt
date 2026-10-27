package com.ismartcoding.plain.features.contacts

import com.ismartcoding.plain.features.telephony.ContactDateType
import kotlinx.serialization.Serializable

@Serializable
data class ContactEventInput(var value: String, var type: ContactDateType, var label: String)

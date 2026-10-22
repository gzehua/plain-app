package com.ismartcoding.plain.features.contacts

import com.ismartcoding.plain.features.telephony.PhoneType
import kotlinx.serialization.Serializable

@Serializable
data class ContactPhoneInput(var value: String, var type: PhoneType, var label: String)

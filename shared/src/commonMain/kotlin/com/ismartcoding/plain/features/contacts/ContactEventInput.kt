package com.ismartcoding.plain.features.contacts

import com.ismartcoding.plain.features.telephony.EventType
import kotlinx.serialization.Serializable

@Serializable
data class ContactEventInput(var value: String, var type: EventType, var label: String)

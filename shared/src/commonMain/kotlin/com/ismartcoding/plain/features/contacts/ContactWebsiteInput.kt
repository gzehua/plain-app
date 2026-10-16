package com.ismartcoding.plain.features.contacts

import com.ismartcoding.plain.features.telephony.EmailType
import com.ismartcoding.plain.features.telephony.EventType
import com.ismartcoding.plain.features.telephony.ImProtocol
import com.ismartcoding.plain.features.telephony.PhoneType
import com.ismartcoding.plain.features.telephony.PostalType
import com.ismartcoding.plain.features.telephony.WebsiteType
import kotlinx.serialization.Serializable

import com.ismartcoding.plain.util.ID

@Serializable
data class ContactWebsiteInput(var value: String, var type: WebsiteType, var label: String)

package com.ismartcoding.plain.features.contacts

import com.ismartcoding.plain.features.telephony.ImProtocol
import kotlinx.serialization.Serializable

@Serializable
data class ContactImInput(var value: String, var protocol: ImProtocol, var customProtocol: String)

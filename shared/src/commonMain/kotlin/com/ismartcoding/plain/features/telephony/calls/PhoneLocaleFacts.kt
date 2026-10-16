package com.ismartcoding.plain.features.telephony.calls

import kotlinx.serialization.Serializable

@Serializable
internal data class PhoneLocaleFacts(val region: String, val locale: String, val available: Boolean)

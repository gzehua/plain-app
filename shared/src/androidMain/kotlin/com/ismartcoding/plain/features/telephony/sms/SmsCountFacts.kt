package com.ismartcoding.plain.features.telephony.sms

import kotlinx.serialization.Serializable

@Serializable
internal data class SmsCountFacts(
    val sms: Int,
    val mms: Int,
)

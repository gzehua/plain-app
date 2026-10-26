package com.ismartcoding.plain.features.telephony.sms

import kotlinx.serialization.Serializable

@Serializable
internal data class MmsProviderRequest(val minimumId: Long, val launchTimeSec: Long)

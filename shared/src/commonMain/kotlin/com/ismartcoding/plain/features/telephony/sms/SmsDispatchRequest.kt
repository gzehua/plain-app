package com.ismartcoding.plain.features.telephony.sms

import kotlinx.serialization.Serializable

@Serializable
data class SmsDispatchRequest(val requestId: String, val number: String, val parts: List<String>, val subscriptionId: Int?)

@Serializable
data class SmsPartSentReport(val requestId: String, val partIndex: Int, val partCount: Int, val success: Boolean, val resultCode: Int)

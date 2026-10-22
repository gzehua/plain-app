package com.ismartcoding.plain.features.telephony.sms

import com.ismartcoding.plain.IData
import kotlinx.serialization.Serializable

@Serializable
data class SmsConversationRowFacts(
    override var id: String,
    val address: String,
    val snippet: String,
    val date: kotlin.time.Instant,
    val messageCount: Int,
    val read: Boolean,
    val addresses: List<String> = if (address.isEmpty()) emptyList() else listOf(address),
) : IData

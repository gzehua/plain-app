package com.ismartcoding.plain.features.sms

import kotlinx.serialization.Serializable

@Serializable
internal data class SmsConversationFacts(
    val items: List<SmsConversationRowFacts>,
    val ownNumbers: List<String>,
    val snippets: Map<String, String>,
)

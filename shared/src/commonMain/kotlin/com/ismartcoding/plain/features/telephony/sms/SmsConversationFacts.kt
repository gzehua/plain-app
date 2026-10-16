package com.ismartcoding.plain.features.telephony.sms

import kotlinx.serialization.Serializable

@Serializable
internal data class SmsConversationFacts(
    val items: List<SmsConversationRowFacts>,
    val ownNumbers: List<String>,
    val snippetCandidates: Map<String, List<SmsSnippetFacts>>,
)

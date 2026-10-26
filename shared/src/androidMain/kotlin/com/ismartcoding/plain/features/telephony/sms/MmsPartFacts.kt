package com.ismartcoding.plain.features.telephony.sms

import kotlinx.serialization.Serializable

@Serializable
data class MmsPartFacts(
    val path: String,
    val contentType: String,
    val name: String,
    val fileName: String,
    val hasData: Boolean,
    val text: String,
)

@Serializable
internal data class SmsSnippetFacts(
    val dateMillis: Long,
    val value: String,
    val parts: List<MmsPartFacts>? = null,
)

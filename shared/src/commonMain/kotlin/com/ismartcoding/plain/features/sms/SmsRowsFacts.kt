package com.ismartcoding.plain.features.sms

import kotlinx.serialization.Serializable

@Serializable
internal data class SmsRowsFacts(
    val items: List<SmsRowFacts>,
    val canonicalAddresses: List<String>,
    val ownNumbers: List<String>,
)

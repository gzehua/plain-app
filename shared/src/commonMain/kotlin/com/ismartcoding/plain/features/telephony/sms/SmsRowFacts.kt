package com.ismartcoding.plain.features.telephony.sms

import com.ismartcoding.plain.IData
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SmsRowFacts(
    override var id: String,
    val body: String,
    val address: String,
    val date: Instant,
    val serviceCenter: String,
    val read: Boolean,
    val threadId: String,
    val type: Int,
    val subscriptionId: Int,
    val isMms: Boolean = false,
    val parts: List<MmsPartFacts> = emptyList(),
    val addressCandidates: List<MmsAddressFacts> = emptyList(),
) : IData

@Serializable
data class MmsAddressFacts(val value: String, val isSender: Boolean)

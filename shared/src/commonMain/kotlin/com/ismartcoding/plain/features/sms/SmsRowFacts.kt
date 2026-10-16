package com.ismartcoding.plain.features.sms

import com.ismartcoding.plain.graphql.models.SmsAttachment

import com.ismartcoding.plain.db.IData
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
    val attachments: List<SmsAttachment> = emptyList(),
    val addressCandidates: List<MmsAddressFacts> = emptyList(),
) : IData

@Serializable
data class MmsAddressFacts(val value: String, val isSender: Boolean)

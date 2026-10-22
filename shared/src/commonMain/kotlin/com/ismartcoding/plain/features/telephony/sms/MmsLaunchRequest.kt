package com.ismartcoding.plain.features.telephony.sms

import com.ismartcoding.plain.models.SmsAttachment

import kotlinx.serialization.Serializable

@Serializable
internal data class MmsLaunchRequest(val number: String, val body: String, val attachments: List<SmsAttachment>)

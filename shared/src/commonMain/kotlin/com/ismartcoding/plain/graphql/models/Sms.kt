package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.SmsType
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Sms(
    var id: String,
    var body: String,
    var address: String,
    var sentAt: Instant,
    var serviceCenter: String,
    var read: Boolean,
    var threadId: String,
    var type: SmsType,
    var subscriptionId: Int,
    var isMms: Boolean,
    var attachments: List<SmsAttachment>,
    var typeCode: Int = 0,
)

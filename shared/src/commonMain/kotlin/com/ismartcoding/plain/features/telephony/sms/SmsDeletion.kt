package com.ismartcoding.plain.features.telephony.sms

import kotlinx.serialization.Serializable

@Serializable
data class SmsDeleteRowsRequest(val ids: List<String>)

@Serializable
data class SmsDeletionReceipt(val deletedIds: List<String>, val failedIds: List<String>)

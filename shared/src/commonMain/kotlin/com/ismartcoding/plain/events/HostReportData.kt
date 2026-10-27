package com.ismartcoding.plain.events

import kotlinx.serialization.Serializable

@Serializable
data class ClipboardReportData(
    val text: String,
    val sensitive: Boolean,
)

@Serializable
data class SmsProviderChangedData(
    val uris: List<String>,
)

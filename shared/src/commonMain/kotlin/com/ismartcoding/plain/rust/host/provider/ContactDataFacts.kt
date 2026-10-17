package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
internal data class ContactDataFacts(
    val rawId: String,
    val mimeType: String,
    val data1: String,
    val data2: String,
    val data3: String,
    val data4: String,
    val data5: String,
    val data6: String,
)

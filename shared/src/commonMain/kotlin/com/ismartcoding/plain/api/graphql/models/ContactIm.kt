package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.features.telephony.ImProtocol
import kotlinx.serialization.Serializable

@Serializable
data class ContactIm(
    var value: String,
    var protocol: ImProtocol,
    var customProtocol: String,
    var typeCode: Int = 0,
)

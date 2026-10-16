package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.features.telephony.ImProtocol
import kotlinx.serialization.Serializable

@Serializable
data class ContactImInput(
    var value: String,
    var protocol: ImProtocol,
    var customProtocol: String,
)

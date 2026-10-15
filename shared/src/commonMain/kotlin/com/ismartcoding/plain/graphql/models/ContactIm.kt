package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.ImProtocol
import kotlinx.serialization.Serializable

@Serializable
data class ContactIm(
    var value: String,
    var protocol: ImProtocol,
    var customProtocol: String,
    var typeCode: Int = 0,
)

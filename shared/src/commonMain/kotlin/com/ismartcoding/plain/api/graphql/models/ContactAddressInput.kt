package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.features.telephony.PostalType
import kotlinx.serialization.Serializable

@Serializable
data class ContactAddressInput(
    var value: String,
    var type: PostalType,
    var label: String,
)

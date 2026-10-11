package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.WebsiteType
import kotlinx.serialization.Serializable

@Serializable
data class ContactWebsiteInput(
    var value: String,
    var type: WebsiteType,
    var label: String,
)

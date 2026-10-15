package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.WebsiteType
import kotlinx.serialization.Serializable

@Serializable
data class ContactWebsite(
    var value: String,
    var type: WebsiteType,
    var label: String,
    var typeCode: Int = 0,
)

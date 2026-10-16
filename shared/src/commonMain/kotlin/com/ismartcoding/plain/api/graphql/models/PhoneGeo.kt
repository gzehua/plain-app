package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class PhoneGeo(
    var country: String,
    var numberType: String,
    var carrier: String,
    var description: String,
)

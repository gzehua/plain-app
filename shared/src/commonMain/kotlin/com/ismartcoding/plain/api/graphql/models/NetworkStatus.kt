package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class NetworkStatus(
    val ipv4Addresses: List<String>,
    val primaryIpv4: String,
    val vpnActive: Boolean,
)

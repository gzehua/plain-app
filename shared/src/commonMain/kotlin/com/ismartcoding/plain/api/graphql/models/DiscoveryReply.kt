package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class DiscoveryReply(
    val id: String,
    val name: String,
    val port: Int,
    val deviceType: String,
    val version: String,
    val platform: String,
    val ips: List<String> = emptyList(),
    val awareSupported: Boolean = false,
    val awareRunning: Boolean = false,
)

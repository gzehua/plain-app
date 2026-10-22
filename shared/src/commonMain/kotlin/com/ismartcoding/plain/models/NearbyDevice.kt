package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class NearbyDevice(
    val id: String,
    val name: String,
    val ips: List<String> = emptyList(),
    val port: Int,
    val deviceType: String,
    val version: String,
    val platform: String,
    val lastSeen: String,
    val discoveryMethods: List<String> = emptyList(),
    val bestIp: String = "",
    val status: String,
)
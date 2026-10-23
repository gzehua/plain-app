package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable



@Serializable
data class PeerAddress(
    val bestIp: String,
    val name: String,
    val baseUrl: String,
    val apiUrl: String,
    val statusWsUrl: String,
)

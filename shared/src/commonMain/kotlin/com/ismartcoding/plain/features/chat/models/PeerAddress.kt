package com.ismartcoding.plain.features.chat.models



@kotlinx.serialization.Serializable
data class PeerAddress(
    val bestIp: String,
    val name: String,
    val baseUrl: String,
    val apiUrl: String,
    val statusWsUrl: String,
)

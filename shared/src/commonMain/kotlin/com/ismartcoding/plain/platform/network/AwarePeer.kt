package com.ismartcoding.plain.platform.network

import kotlinx.serialization.Serializable

@Serializable
data class AwarePeer(val id: String, val port: Int?)

package com.ismartcoding.plain.features.system

import kotlinx.serialization.Serializable

@Serializable
internal data class AppFacts(
    val appDir: String,
    val deviceType: String,
    val capabilities: List<String>,
    val buildChannel: String,
    val permissions: List<String>,
    val downloadsDir: String,
    val debug: Boolean,
)

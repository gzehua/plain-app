package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
internal data class PermissionFacts(
    val granted: Map<String, Boolean>,
)

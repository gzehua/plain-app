package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
internal data class PackageInstallFacts(
    val packageName: String,
    val lastUpdateTime: String?,
    val isNew: Boolean,
)

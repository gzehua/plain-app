package com.ismartcoding.plain.platform.system

import kotlinx.serialization.Serializable

@Serializable
enum class PackageType {
    SYSTEM,
    USER;
}

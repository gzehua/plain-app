package com.ismartcoding.plain.platform.system

import kotlinx.serialization.Serializable

@Serializable
enum class DeviceType {
    COMPUTER,
    PHONE,
    TABLET,
    TV,
    NAS,
    OTHER;
}

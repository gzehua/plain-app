package com.ismartcoding.plain.platform.system

import kotlinx.serialization.Serializable

@Serializable
enum class DevicePlatform {
    ANDROID, IOS, MACOS, WINDOWS, LINUX
}

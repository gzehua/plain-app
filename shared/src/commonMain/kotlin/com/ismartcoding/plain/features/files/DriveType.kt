package com.ismartcoding.plain.features.files

import kotlinx.serialization.Serializable

@Serializable
enum class DriveType {
    INTERNAL_STORAGE,
    SDCARD,
    USB_STORAGE,
    APP,
}

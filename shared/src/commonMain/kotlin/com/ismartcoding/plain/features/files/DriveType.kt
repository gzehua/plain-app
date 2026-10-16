package com.ismartcoding.plain.features.files

@kotlinx.serialization.Serializable
enum class DriveType {
    INTERNAL_STORAGE,
    SDCARD,
    USB_STORAGE,
    APP,
}

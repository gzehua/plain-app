package com.ismartcoding.plain.platform

actual fun getDeviceCapabilities(): List<Capability> = listOf(Capability.FILE_EXPORT, Capability.NOTES, Capability.FEEDS, Capability.POMODORO, Capability.CLIPBOARD)

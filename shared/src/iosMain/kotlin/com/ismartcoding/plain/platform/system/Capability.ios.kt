package com.ismartcoding.plain.platform.system

actual fun getDeviceCapabilities(): List<Capability> = listOf(Capability.FILE_EXPORT, Capability.NOTES, Capability.FEEDS, Capability.POMODORO, Capability.CLIPBOARD)

package com.ismartcoding.plain.platform

actual fun getDeviceCapabilities(): List<Capability> = listOf(Capability.NOTES, Capability.FEEDS, Capability.POMODORO, Capability.CLIPBOARD)

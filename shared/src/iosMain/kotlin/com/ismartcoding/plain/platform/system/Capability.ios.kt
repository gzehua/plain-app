package com.ismartcoding.plain.platform.system

actual fun getDeviceCapabilities(): List<Capability> = listOf(Capability.BACKGROUND_WAKE, Capability.FILE_EXPORT, Capability.NOTES, Capability.FEEDS, Capability.POMODORO, Capability.CLIPBOARD).let { if (isAppForegrounded()) it + Capability.BACKGROUND_TIMER else it }

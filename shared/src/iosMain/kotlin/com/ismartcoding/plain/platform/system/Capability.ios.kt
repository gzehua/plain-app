package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.platform.network.isBluetoothSupported

actual fun getDeviceCapabilities(): List<Capability> = buildList {
    addAll(listOf(
        Capability.PEER_DISCOVERY, Capability.FILE_BROWSER, Capability.SOUND_METER,
        Capability.BACKGROUND_WAKE, Capability.FILE_EXPORT, Capability.NOTES,
        Capability.FEEDS, Capability.POMODORO, Capability.CLIPBOARD,
    ))
    if (isBluetoothSupported()) add(Capability.BLE_DISCOVERY)
    if (isAppForegrounded()) add(Capability.BACKGROUND_TIMER)
}

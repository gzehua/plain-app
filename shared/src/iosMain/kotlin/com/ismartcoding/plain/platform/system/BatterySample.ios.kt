package com.ismartcoding.plain.platform.system

import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceBatteryState

internal actual fun readBatterySample(): BatterySample {
    val device = UIDevice.currentDevice
    device.batteryMonitoringEnabled = true
    return BatterySample(
        level = device.batteryLevel.toDouble(),
        scale = 1.0,
        charging = device.batteryState == UIDeviceBatteryState.UIDeviceBatteryStateCharging,
    )
}

package com.ismartcoding.plain.platform.system

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.ismartcoding.plain.appContext

internal actual fun readBatterySample(): BatterySample {
    val intent = appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    return BatterySample(
        level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)?.toDouble(),
        scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)?.toDouble(),
        charging = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING,
        temperatureTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)?.takeUnless { it == Int.MIN_VALUE },
    )
}

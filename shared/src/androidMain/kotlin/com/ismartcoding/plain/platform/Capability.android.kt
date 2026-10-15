package com.ismartcoding.plain.platform

import android.os.Build
import com.ismartcoding.plain.buildChannel
import com.ismartcoding.plain.enums.AppChannelType

actual fun getDeviceCapabilities(): List<Capability> = Capability.entries.filter { capability ->
    when (capability) {
        Capability.MEDIA_SCAN, Capability.LAN_SHARE, Capability.DISK_MANAGER -> false
        Capability.MEDIA_TRASH, Capability.MEDIA_FAVORITE -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        Capability.MEDIA_MOVE, Capability.MIRROR_AUDIO -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        Capability.PACKAGES, Capability.SMS, Capability.CALLS, Capability.NOTIFICATIONS, Capability.DONATION -> buildChannel != AppChannelType.GOOGLE.name
        Capability.APP_UPDATES -> buildChannel == AppChannelType.GITHUB.name
        Capability.IMAGE_EDITOR -> isDebugBuild()
        else -> true
    }
}

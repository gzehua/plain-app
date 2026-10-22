package com.ismartcoding.plain.platform.network

import com.ismartcoding.plain.platform.system.IosPlatformRegistry

actual fun getNetworkType(): NetworkType = NetworkType.valueOf(IosPlatformRegistry.networkType())

internal actual fun vpnActive(): Boolean = IosPlatformRegistry.vpnActive()

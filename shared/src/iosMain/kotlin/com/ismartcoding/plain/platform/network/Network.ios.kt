package com.ismartcoding.plain.platform.network



actual fun getNetworkType(): NetworkType = NetworkType.valueOf(com.ismartcoding.plain.platform.system.IosPlatformRegistry.networkType())

internal actual fun vpnActive(): Boolean = error("VPN state is sampled by Rust")

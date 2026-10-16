package com.ismartcoding.plain.platform.network

enum class NetworkType { WIFI, CELLULAR, ETHERNET, NONE, OTHER }

/**
 * Current primary network type.
 */
expect fun getNetworkType(): NetworkType

internal expect fun vpnActive(): Boolean

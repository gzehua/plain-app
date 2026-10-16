package com.ismartcoding.plain.platform.network

import com.ismartcoding.plain.platform.isTPlus
import com.ismartcoding.plain.rust.http.get

import com.ismartcoding.plain.platform.network.aware.WifiAwareTransport
import com.ismartcoding.plain.wifiAwareManager

actual val isWifiAwareSupported: Boolean
    get() = isTPlus() && WifiAwareTransport.isSupported()

actual fun getAwareDataInterfaces(): Int? =
    if (isTPlus()) runCatching { wifiAwareManager.getCharacteristics()?.getNumberOfSupportedDataInterfaces() }.getOrNull() else null

actual fun getAwareDataPaths(): Int? =
    if (isTPlus()) runCatching { wifiAwareManager.getCharacteristics()?.getNumberOfSupportedDataPaths() }.getOrNull() else null

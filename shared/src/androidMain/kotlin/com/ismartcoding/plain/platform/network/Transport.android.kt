package com.ismartcoding.plain.platform.network

import com.ismartcoding.plain.platform.isTPlus

import com.ismartcoding.plain.platform.network.aware.WifiAwareTransport

actual val isWifiAwareSupported: Boolean
    get() = isTPlus() && WifiAwareTransport.isSupported()

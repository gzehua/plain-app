package com.ismartcoding.plain.platform.network

import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.isTPlus

actual fun wifiAwarePermission(): Permission? =
    if (!isWifiAwareSupported) null
    else if (isTPlus()) Permission.NEARBY_WIFI_DEVICES
    else Permission.ACCESS_FINE_LOCATION

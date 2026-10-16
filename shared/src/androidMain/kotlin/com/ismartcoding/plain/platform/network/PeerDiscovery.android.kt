package com.ismartcoding.plain.platform.network

import com.ismartcoding.plain.platform.isTPlus

import android.Manifest
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.lib.extensions.hasPermission

actual fun canShowNotifications(): Boolean {
    return if (isTPlus()) {
        appContext.hasPermission(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        androidx.core.app.NotificationManagerCompat.from(appContext).areNotificationsEnabled()
    }
}

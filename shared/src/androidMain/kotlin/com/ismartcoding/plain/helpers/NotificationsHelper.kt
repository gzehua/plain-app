package com.ismartcoding.plain.helpers

import com.ismartcoding.plain.AndroidTempData
import com.ismartcoding.plain.TempData

import com.ismartcoding.plain.graphql.models.Notification as AppNotification
import com.ismartcoding.plain.preferences.*
import android.content.Context
object NotificationsHelper {
    suspend fun filterNotificationsAsync(context: Context): List<AppNotification> {
        return com.ismartcoding.plain.platform.filterNotificationsAsync()
    }
}

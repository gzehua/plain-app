package com.ismartcoding.plain.platform.notifications

import com.ismartcoding.plain.AndroidTempData
import com.ismartcoding.plain.app.TempData

import com.ismartcoding.plain.api.graphql.models.Notification as AppNotification
import com.ismartcoding.plain.preferences.*
import android.content.Context
object NotificationsHelper {
    suspend fun filterNotificationsAsync(context: Context): List<AppNotification> {
        return com.ismartcoding.plain.platform.notifications.filterNotificationsAsync()
    }
}

@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.ismartcoding.plain.platform.notifications

import com.ismartcoding.plain.lib.logcat.LogCat
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

internal fun sendLocalNotification(
    identifier: String,
    title: String,
    body: String,
    threadIdentifier: String = "",
    playSound: Boolean = true,
) {
    val center = UNUserNotificationCenter.currentNotificationCenter()
    center.getNotificationSettingsWithCompletionHandler { settings ->
        when (settings?.authorizationStatus) {
            UNAuthorizationStatusAuthorized, UNAuthorizationStatusProvisional, UNAuthorizationStatusEphemeral -> {
                val content = UNMutableNotificationContent().apply {
                    setTitle(title)
                    setBody(body)
                    setThreadIdentifier(threadIdentifier)
                    if (playSound) setSound(UNNotificationSound.defaultSound)
                }
                val request = UNNotificationRequest.requestWithIdentifier(identifier, content, trigger = null)
                center.addNotificationRequest(request) { error ->
                    if (error != null) LogCat.e("Send local notification $identifier: ${error.localizedDescription}")
                }
            }
            else -> Unit
        }
    }
}

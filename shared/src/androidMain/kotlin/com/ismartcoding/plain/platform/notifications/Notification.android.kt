package com.ismartcoding.plain.platform.notifications

import com.ismartcoding.plain.models.Notification as AppNotification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.ui.platform.notifications.NotificationHelper
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.services.PNotificationListenerService
import kotlinx.coroutines.delay

internal suspend fun notificationFacts(): List<AppNotification> = withIO {
    PNotificationListenerService.notifications.toList()
}

internal suspend fun cancelNotificationFacts(ids: Set<String>): Set<String> = withIO {
    val service = PNotificationListenerService.connectedService ?: return@withIO emptySet()
    if (!service.isConnected) return@withIO emptySet()
    val active = service.activeNotifications.associateBy { it.key }
    val submitted = ids.filter { active[it]?.isClearable == true && runCatching { service.cancelNotification(it) }.isSuccess }.toSet()
    if (submitted.isEmpty()) return@withIO emptySet()
    var remaining = submitted
    repeat(20) {
        delay(25)
        val keys = service.activeNotifications.map { it.key }.toSet()
        remaining = submitted.intersect(keys)
        if (remaining.isEmpty()) return@withIO submitted
    }
    submitted - remaining
}

internal fun replyNotification(id: String, actionIndex: Int, text: String): Boolean {
    val actions = PNotificationListenerService.notificationActions[id] ?: return false
    // Only consider reply-capable actions (those with remoteInputs)
    val replyActions = actions.filter { it.remoteInputs != null && it.remoteInputs.isNotEmpty() }
    val action = replyActions.getOrNull(actionIndex) ?: return false
    val remoteInputs = action.remoteInputs ?: return false
    val remoteInput = remoteInputs.first()
    val intent = Intent()
    val bundle = Bundle()
    bundle.putCharSequence(remoteInput.resultKey, text)
    RemoteInput.addResultsToIntent(remoteInputs, intent, bundle)
    action.actionIntent.send(appContext, 0, intent)
    return true
}

internal actual fun postWebLoginNotification(title: String, description: String) {
    NotificationHelper.postWebLoginNotification(appContext, title, description)
}

actual fun sendChatMessageNotification(targetId: String, targetName: String, messageText: String) {
    NotificationHelper.sendChatMessageNotification(
        context = appContext,
        targetId = targetId,
        targetName = targetName,
        messageText = messageText,
    )
}

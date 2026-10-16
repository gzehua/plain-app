package com.ismartcoding.plain.platform.notifications

import com.ismartcoding.plain.features.packages.PackagesClient
import com.ismartcoding.plain.api.graphql.models.Notification as AppNotification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import com.ismartcoding.plain.AndroidTempData
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.packageManager
import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.ui.platform.notifications.NotificationHelper
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.services.PNotificationListenerService

actual fun toggleNotificationListener(enabled: Boolean) {
    PNotificationListenerService.toggle(appContext, enabled)
}

actual suspend fun getNotificationApp(packageName: String): DNotificationApp? = withIO {
    runCatching {
        val info = packageManager.getApplicationInfo(packageName, 0)
        DNotificationApp(id = info.packageName, name = packageManager.getApplicationLabel(info).toString())
    }.getOrNull()
}

actual suspend fun getAllNotificationApps(): List<DNotificationApp> = withIO {
    val ownPackage = appContext.packageName
    PackagesClient.search("", Int.MAX_VALUE, 0, FileSortBy.NAME_ASC)
        .filter { it.id != ownPackage }
        .map { DNotificationApp(id = it.id, name = it.name) }
}

actual suspend fun notificationFacts(): List<AppNotification> = withIO {
    AndroidTempData.notifications.toList()
}

actual suspend fun cancelNotificationFacts(ids: Set<String>): Set<String> = withIO {
    val service = PNotificationListenerService.connectedService ?: return@withIO emptySet()
    if (!service.isConnected) return@withIO emptySet()
    val active = service.activeNotifications.associateBy { it.key }
    val submitted = ids.filter { active[it]?.isClearable == true && runCatching { service.cancelNotification(it) }.isSuccess }.toSet()
    if (submitted.isEmpty()) return@withIO emptySet()
    var remaining = submitted
    repeat(20) {
        kotlinx.coroutines.delay(25)
        val keys = service.activeNotifications.map { it.key }.toSet()
        remaining = submitted.intersect(keys)
        if (remaining.isEmpty()) return@withIO submitted
    }
    submitted - remaining
}

actual fun replyNotification(id: String, actionIndex: Int, text: String): Boolean {
    val actions = AndroidTempData.notificationActions[id] ?: return false
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

actual fun sendWebLoginNotification(
    browserName: String,
    browserVersion: String,
    osName: String,
    osVersion: String,
    clientIp: String,
) {
    NotificationHelper.sendWebLoginNotification(
        appContext,
        browserName,
        browserVersion,
        osName,
        osVersion,
        clientIp,
    )
}

actual fun generateNotificationId(): Int = NotificationHelper.generateId()

actual fun sendChatMessageNotification(targetId: String, targetName: String, messageText: String) {
    NotificationHelper.sendChatMessageNotification(
        context = appContext,
        targetId = targetId,
        targetName = targetName,
        messageText = messageText,
    )
}

package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.notifications.cancelNotificationFacts
import com.ismartcoding.plain.platform.notifications.notificationFacts
import com.ismartcoding.plain.platform.notifications.replyNotification
import com.ismartcoding.plain.rust.host.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

internal object SystemNotificationsHost {
    internal suspend fun handle(method: String, params: JsonObject): HostResult = when (method) {
        "systemNotificationFacts" -> hostResult(notificationFacts())
        "systemCancelNotifications" -> hostResult(cancelNotificationFacts(
            JsonHelper.jsonDecodeFromElement<IdsRequest>(params).ids.toSet()))
        "systemReplyNotification" -> {
            val request = JsonHelper.jsonDecodeFromElement<ReplyNotificationRequest>(params)
            hostResult(replyNotification(request.id, request.actionIndex, request.text))
        }
        else -> error("Unsupported provider operation")
    }
}

@Serializable
private data class ReplyNotificationRequest(val id: String, val actionIndex: Int, val text: String)

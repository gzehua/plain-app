package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.notifications.notificationFacts
import kotlinx.serialization.json.*

internal object SystemNotificationsHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemNotificationFacts" -> JsonHelper.jsonEncodeToElement(notificationFacts())
        "systemCancelNotifications" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.notifications.cancelNotificationFacts(
            params.getValue("ids").jsonArray.map { it.jsonPrimitive.content }.toSet()))
        "systemReplyNotification" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.notifications.replyNotification(
            params.getValue("id").jsonPrimitive.content, params.getValue("actionIndex").jsonPrimitive.int, params.getValue("text").jsonPrimitive.content))
        else -> error("Unsupported provider operation")
    }
}

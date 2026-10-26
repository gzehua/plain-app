@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.ismartcoding.plain.platform.notifications

import platform.Foundation.NSUUID

internal actual fun postWebLoginNotification(title: String, description: String) {
    sendLocalNotification(NSUUID().UUIDString, title, description)
}

actual fun sendChatMessageNotification(targetId: String, targetName: String, messageText: String) {
    sendLocalNotification(
        identifier = "chat_$targetId",
        title = targetName,
        body = messageText,
        threadIdentifier = "chat_$targetId",
    )
}

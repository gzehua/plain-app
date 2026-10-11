package com.ismartcoding.plain.chat

import com.ismartcoding.plain.TempData
import com.ismartcoding.plain.chat.data.ChatTarget
import com.ismartcoding.plain.chat.data.ChatTargetType
import com.ismartcoding.plain.chat.models.ChatChannelRecord
import com.ismartcoding.plain.chat.models.PeerRecord
import com.ismartcoding.plain.chat.models.getMessagePreview
import com.ismartcoding.plain.events.ChatMessageNotificationEvent
import com.ismartcoding.plain.graphql.models.ChatChannel
import com.ismartcoding.plain.graphql.models.ChatItem
import com.ismartcoding.plain.graphql.models.Peer
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.peer_chat
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.platform.LocaleHelper
import com.ismartcoding.plain.platform.canShowNotifications

object ChatMessageReceiver {

    suspend fun applyCommitted(item: ChatItem, fromPeer: PeerRecord, fromChannel: ChatChannelRecord?) = withIO {
        val fromChannelId = item.channelId.orEmpty()
        val fromPeerId = item.fromId
        ChatViewModel.onMessagesCreated(
            target = if (fromChannelId.isNotEmpty()) {
                ChatTarget(fromChannelId, ChatTargetType.CHANNEL)
            } else {
                ChatTarget(fromPeerId, ChatTargetType.PEER)
            },
            items = listOf(item),
        )
        ChatManager.refreshLatestChats()
        emitNotificationIfNeeded(item, fromPeer, fromChannel)
    }

    private fun emitNotificationIfNeeded(
        item: ChatItem,
        fromPeer: PeerRecord,
        fromChannel: ChatChannelRecord?,
    ) {
        if (!canShowNotifications()) return
        val preview = item.getMessagePreview()
        val (targetId, targetName, messageText) = if (fromChannel == null) {
            NotificationPayload(
                targetId = "peer:${fromPeer.id}",
                targetName = fromPeer.name.ifEmpty { LocaleHelper.getString(Res.string.peer_chat) },
                messageText = preview,
            )
        } else {
            NotificationPayload(
                targetId = "channel:${fromChannel.id}",
                targetName = fromChannel.name.ifEmpty { LocaleHelper.getString(Res.string.peer_chat) },
                messageText = "${fromPeer.name}: $preview",
            )
        }
        if (TempData.activeToId == targetId) return
        sendEvent(ChatMessageNotificationEvent(
            targetId = targetId,
            targetName = targetName,
            messageText = messageText,
        ))
    }
}

private data class NotificationPayload(
    val targetId: String,
    val targetName: String,
    val messageText: String,
)

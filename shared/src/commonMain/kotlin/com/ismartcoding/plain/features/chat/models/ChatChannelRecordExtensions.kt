package com.ismartcoding.plain.features.chat.models

import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.ui.chat.peer.PeerCacher
import com.ismartcoding.plain.features.chat.ChatChannelStatus
import com.ismartcoding.plain.platform.system.DeviceType
import com.ismartcoding.plain.api.graphql.models.ChatChannelMember
import com.ismartcoding.plain.platform.system.getDeviceIP4s
import com.ismartcoding.plain.preferences.*

fun ChatChannelMember.isMe(): Boolean = peerId == TempData.clientId

fun ChatChannelRecord.isOwnedByMe(): Boolean {
    return ownerId == "me" || ownerId == TempData.clientId
}

fun ChatChannelRecord.canLeave(): Boolean = !isOwnedByMe() && isJoined()

fun ChatChannelRecord.canDeleteFromThisDevice(): Boolean =
    isOwnedByMe() || status == ChatChannelStatus.LEFT || status == ChatChannelStatus.KICKED

fun mePeer(): PeerRecord = PeerRecord(
    id = TempData.clientId,
    name = TempData.deviceName.value,
    ip = getDeviceIP4s().joinToString(","),
    port = UserPrefs.httpsPort.value,
    deviceType = DeviceType.PHONE,
)

fun ChatChannelRecord.getOwner(): PeerRecord? {
    return if (isOwnedByMe()) mePeer() else PeerCacher.getPeer(ownerId)
}

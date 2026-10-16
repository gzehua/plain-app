package com.ismartcoding.plain.chat.models

import com.ismartcoding.plain.TempData
import com.ismartcoding.plain.chat.peer.PeerCacher
import com.ismartcoding.plain.enums.ChatChannelStatus
import com.ismartcoding.plain.enums.DeviceType
import com.ismartcoding.plain.graphql.models.ChatChannelMember
import com.ismartcoding.plain.platform.getDeviceIP4s
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

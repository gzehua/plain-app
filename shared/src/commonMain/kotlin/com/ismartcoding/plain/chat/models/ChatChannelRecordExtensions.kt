package com.ismartcoding.plain.chat.models

import com.ismartcoding.plain.TempData
import com.ismartcoding.plain.chat.peer.PeerCacher
import com.ismartcoding.plain.chat.peer.RustPeerStore
import com.ismartcoding.plain.enums.ChatChannelStatus
import com.ismartcoding.plain.enums.DeviceType
import com.ismartcoding.plain.graphql.models.ChatChannelMember
import com.ismartcoding.plain.helpers.Base64Lenient
import com.ismartcoding.plain.helpers.SignatureHelper
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.platform.getDeviceIP4s
import com.ismartcoding.plain.platform.verifyEd25519Signature
import com.ismartcoding.plain.preferences.*
import kotlin.io.encoding.ExperimentalEncodingApi

fun ChatChannelMember.isMe(): Boolean = peerId == TempData.clientId

fun ChatChannelRecord.isOwnedByMe(): Boolean {
    return ownerId == "me" || ownerId == TempData.clientId
}

fun ChatChannelRecord.canLeave(): Boolean = !isOwnedByMe() && isJoined()

fun ChatChannelRecord.canDeleteFromThisDevice(): Boolean =
    isOwnedByMe() || status == ChatChannelStatus.LEFT || status == ChatChannelStatus.KICKED

fun ChatChannelRecord.getRecipientIds(): List<String> {
    return joinedMembers()
        .map { it.peerId }
        .distinct()
        .filter { it != TempData.clientId }
}

suspend fun ChatChannelRecord.getPeersAsync(): List<PeerRecord> = withIO {
    val ids = memberIds()
    val dbPeers = RustPeerStore.getByIds(ids).associateBy { it.id }
    ids.mapNotNull { peerId ->
        if (peerId == TempData.clientId) {
            PeerRecord(
                id = peerId,
                name = TempData.deviceName.value,
                ip = getDeviceIP4s().joinToString(","),
                port = UserPrefs.httpsPort.value,
                publicKey = SignatureHelper.getRawPublicKeyBase64Async(),
                deviceType = DeviceType.PHONE,
            )
        } else {
            dbPeers[peerId]
        }
    }
}

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

@OptIn(ExperimentalEncodingApi::class)
fun verifyEd25519Signature(
    publicKeyBase64: String,
    payload: String,
    signatureBase64: String,
): Boolean {
    if (publicKeyBase64.isEmpty() || signatureBase64.isEmpty()) return true
    return try {
        val publicKey = Base64Lenient.decode(publicKeyBase64)
        val signature = Base64Lenient.decode(signatureBase64)
        verifyEd25519Signature(publicKey, payload.encodeToByteArray(), signature)
    } catch (_: Exception) {
        false
    }
}

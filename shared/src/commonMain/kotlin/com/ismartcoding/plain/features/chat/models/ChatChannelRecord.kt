package com.ismartcoding.plain.features.chat.models

import com.ismartcoding.plain.features.chat.ChatChannelStatus
import com.ismartcoding.plain.api.graphql.models.ChatChannelMember
import com.ismartcoding.plain.lib.TimeHelper
import com.ismartcoding.plain.lib.generateId
import kotlin.time.Instant

@kotlinx.serialization.Serializable
data class ChatChannelRecord(
    @kotlinx.serialization.Required var id: String = generateId(),
    @kotlinx.serialization.Required var name: String = "",
    @kotlinx.serialization.Required var key: String = "",
    /** peer.id of the device that created this channel.
     *  Sentinel value "me" when this device is the owner. */
    @kotlinx.serialization.Required @kotlinx.serialization.SerialName("owner_id") var ownerId: String = "",
    /** All channel members (both joined and pending).
     *  Each entry carries only the peer id and membership status;
     *  other metadata (name, publicKey, IP, port) lives in the `peers` table. */
    @kotlinx.serialization.Required @kotlinx.serialization.Serializable(with = ChannelMembersSerializer::class) var members: List<ChatChannelMember> = emptyList(),
    /** Monotonically increasing counter; incremented on every mutation.
     *  Receivers ignore updates whose version ≤ their local version. */
    @kotlinx.serialization.Required var version: Long = 0,
    @kotlinx.serialization.Required var status: ChatChannelStatus = ChatChannelStatus.JOINED,

    @kotlinx.serialization.Required @kotlinx.serialization.SerialName("created_at") var createdAt: Instant = TimeHelper.now(),
    @kotlinx.serialization.Required @kotlinx.serialization.SerialName("updated_at") var updatedAt: Instant = TimeHelper.now(),
) {

    // ── Helpers ─────────────────────────────────────────────────────

    fun memberIds(): List<String> = members.map { it.peerId }
    fun memberIdsNotMe(myId: String): List<String> = members.filter { it.peerId != myId }.map { it.peerId }

    fun joinedMembers(): List<ChatChannelMember> = members.filter { it.isJoined() }

    fun pendingMembers(): List<ChatChannelMember> = members.filter { it.isPending() }

    fun hasMember(peerId: String): Boolean = members.any { it.peerId == peerId }

    fun findMember(peerId: String): ChatChannelMember? = members.find { it.peerId == peerId }

    fun isJoined(): Boolean = status == ChatChannelStatus.JOINED
}

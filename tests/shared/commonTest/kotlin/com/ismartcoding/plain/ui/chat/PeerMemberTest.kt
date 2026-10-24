package com.ismartcoding.plain.ui.chat

import com.ismartcoding.plain.features.chat.ChannelMemberStatus
import com.ismartcoding.plain.models.ChatChannelMember
import com.ismartcoding.plain.platform.system.DeviceType
import com.ismartcoding.plain.ui.chat.components.PeerMember
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PeerMemberTest {
    @Test
    fun localOwnerIsVisibleWithoutAPeerRecord() {
        val member = PeerMember(
            ChatChannelMember("local"), "This phone", DeviceType.PHONE, isOwner = true,
        )

        assertEquals("local", member.id)
        assertEquals("This phone", member.displayName())
        assertEquals(DeviceType.PHONE, member.deviceType)
        assertNull(member.peer)
        assertTrue(member.isSelf)
        assertTrue(member.isOwner)
        assertTrue(member.isJoined())
        assertFalse(member.isPending())
    }

    @Test
    fun localInviteRetainsItsPendingStateWithoutAPeerRecord() {
        val member = PeerMember(
            ChatChannelMember("local", ChannelMemberStatus.PENDING), "This tablet", DeviceType.TABLET,
        )

        assertTrue(member.isSelf)
        assertTrue(member.isPending())
        assertFalse(member.isJoined())
        assertFalse(member.isOwner)
    }
}

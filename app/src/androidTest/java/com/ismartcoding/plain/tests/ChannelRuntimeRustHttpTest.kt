package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.TempData
import com.ismartcoding.plain.chat.channel.ChannelCacher
import com.ismartcoding.plain.chat.channel.ChannelManager
import com.ismartcoding.plain.chat.channel.RustChannelStore
import com.ismartcoding.plain.chat.models.PeerRecord
import com.ismartcoding.plain.chat.peer.RustPeerStore
import com.ismartcoding.plain.enums.ChannelMemberStatus
import com.ismartcoding.plain.enums.PeerStatus
import com.ismartcoding.plain.graphql.models.ChatChannelMember
import java.util.UUID
import kotlin.io.encoding.Base64
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChannelRuntimeRustHttpTest {
    @Test
    fun lifecycleRunsThroughRustAndOfflineAcceptKeepsInvitationPending() = runBlocking {
        val prefix = "synthetic-channel-runtime-${UUID.randomUUID()}"
        val unknown = "$prefix-missing"
        val owner = PeerRecord(id = "$prefix-owner", name = prefix, status = PeerStatus.CHANNEL, ip = "127.0.0.1", port = 1)
        val channel = ChannelManager.createChannel(" $prefix ")
        var foreignId: String? = null
        try {
            assertEquals(prefix, channel.name)
            assertEquals(TempData.clientId, channel.ownerId)
            assertEquals(32, Base64.decode(channel.key).size)
            val renamed = ChannelManager.renameChannel(channel.id, "$prefix renamed")
            assertEquals(2L, renamed.version)
            val invited = ChannelManager.inviteMember(channel.id, unknown)
            assertEquals(ChannelMemberStatus.PENDING, invited.members.single { it.peerId == unknown }.status)
            assertTrue(runCatching { ChannelManager.resendInvite(channel.id, unknown) }.isFailure)
            assertTrue(runCatching { ChannelManager.leaveChannel(channel.id) }.isFailure)
            ChannelManager.kickMember(channel.id, unknown)
            assertFalse(RustChannelStore.getById(channel.id)!!.members.any { it.peerId == unknown })
            ChannelManager.deleteChannel(channel.id)
            assertNull(RustChannelStore.getById(channel.id))
            RustPeerStore.insert(owner)
            val foreign = RustChannelStore.create(prefix).copy(
                ownerId = owner.id,
                members = listOf(ChatChannelMember(owner.id), ChatChannelMember(TempData.clientId, ChannelMemberStatus.PENDING)),
            )
            foreignId = foreign.id
            RustChannelStore.update(foreign)
            assertFalse(ChannelManager.acceptInvite(foreign.id).isSuccess)
            assertEquals(ChannelMemberStatus.PENDING, RustChannelStore.getById(foreign.id)!!.members.single { it.peerId == TempData.clientId }.status)
            ChannelManager.declineInvite(foreign.id)
            assertNull(RustChannelStore.getById(foreign.id))
        } finally {
            RustChannelStore.remove(channel.id)
            foreignId?.let { RustChannelStore.remove(it) }
            RustPeerStore.delete(owner.id)
            ChannelCacher.load()
        }
    }
}

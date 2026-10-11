package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.chat.RustChatFixtures
import com.ismartcoding.plain.chat.RustChatStore
import com.ismartcoding.plain.chat.models.MessageContent
import com.ismartcoding.plain.chat.models.MessageDeliveryResult
import com.ismartcoding.plain.chat.models.MessageFile
import com.ismartcoding.plain.chat.models.MessageFiles
import com.ismartcoding.plain.chat.models.MessageText
import com.ismartcoding.plain.chat.models.MessageType
import com.ismartcoding.plain.chat.models.PeerRecord
import com.ismartcoding.plain.chat.models.toJSONString
import com.ismartcoding.plain.chat.peer.RustPeerStore
import com.ismartcoding.plain.db.*
import com.ismartcoding.plain.enums.ChatStatus
import java.util.UUID
import kotlin.io.encoding.Base64
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageLifecycleRustHttpTest {
    @Test
    fun rustCreatesReceivesDeduplicatesAndMergesDeliveryFromCurrentStoredState() = runBlocking {
        val id = "synthetic-message-lifecycle-${UUID.randomUUID()}"
        val chats = mutableListOf<String>()
        val content = MessageContent(MessageType.TEXT, MessageText("fixture ' 中文"))
        try {
            RustPeerStore.insert(PeerRecord(id = id, name = id))
            val local = RustChatFixtures.create("local", "", content)
            chats += local.id
            assertEquals("me", local.fromId)
            assertEquals(ChatStatus.SENT, local.status)
            val remote = RustChatFixtures.create(id, "", content)
            chats += remote.id
            assertEquals(ChatStatus.PENDING, remote.status)
            val partial = requireNotNull(RustChatFixtures.delivery(remote.id, listOf(MessageDeliveryResult(id, id), MessageDeliveryResult("second", "second", "offline"))))
            assertEquals(ChatStatus.PARTIAL, partial.status)
            val retry = requireNotNull(RustChatFixtures.delivery(remote.id, listOf(MessageDeliveryResult("second", "second")), retry = true))
            assertEquals(ChatStatus.SENT, retry.status)
            assertEquals(2, retry.parseStatusData()!!.results.size)
            assertEquals(remote.createdAt, retry.createdAt)
            assertEquals(remote.content, retry.content)
            val signature = Base64.encode(ByteArray(64) { 7 })
            val timestamp = System.currentTimeMillis()
            val received = requireNotNull(RustChatFixtures.receive(id, "", content.toJSONString(), signature, timestamp))
            chats += received.chat.id
            assertEquals(id, received.peer.id)
            assertEquals("me", received.chat.toId)
            assertEquals(ChatStatus.SENT, received.chat.status)
            assertNull(RustChatFixtures.receive(id, "", content.toJSONString(), signature, timestamp))
            RustChatFixtures.delete(received.chat.id)
            assertNull(RustChatFixtures.receive(id, "", content.toJSONString(), signature, timestamp))
            RustChatFixtures.delete(remote.id)
            assertNull(RustChatFixtures.delivery(remote.id, emptyList()))
        } finally {
            RustChatFixtures.deleteByIds(chats)
            RustPeerStore.delete(id)
            com.ismartcoding.plain.chat.peer.PeerCacher.load()
            com.ismartcoding.plain.chat.ChatCacher.load()
        }
    }
    @Test
    fun rustFileCommandsBindSharedOwnershipAndIgnoreTheCurrentUiTarget() = runBlocking {
        val manager = com.ismartcoding.plain.chat.ChatManager
        val local = com.ismartcoding.plain.chat.data.ChatTarget.parseId("peer:local")
        val wrongTarget = com.ismartcoding.plain.chat.data.ChatTarget.parseId("peer:synthetic-other")
        val bytes = "synthetic-${UUID.randomUUID()}".toByteArray()
        val owned = com.ismartcoding.plain.helpers.AppFileStore.importBytes(bytes, "text/plain")
        val file = MessageFile(id = owned.id, uri = "content://synthetic", fileName = "fixture.txt", size = 0)
        val chats = mutableListOf<String>()
        try {
            val first = manager.insertFilesImmediate(local, listOf(file), false).also { chats += it.id }
            val second = manager.insertFilesImmediate(local, listOf(file), false).also { chats += it.id }
            val imported = file.copy(uri = com.ismartcoding.plain.helpers.AppFileStore.toFidUri(owned))
            chats.forEach { manager.updateFilesMessage(it, listOf(imported)) }
            assertEquals(2, com.ismartcoding.plain.helpers.AppFileStore.getById(owned.id)!!.refCount)
            val saved = manager.updateFilesMessage(first.id, listOf(imported))!!
            assertEquals(ChatStatus.SENT, saved.status)
            assertEquals(bytes.size.toLong(), (saved.messageContent.value as MessageFiles).items.single().size)
            RustChatFixtures.delete(first.id)
            assertEquals(1, com.ismartcoding.plain.helpers.AppFileStore.getById(owned.id)!!.refCount)
            assertTrue(java.io.File(com.ismartcoding.plain.helpers.AppFileStore.resolveUri(imported.uri)).exists())
            RustChatFixtures.delete(second.id)
            assertNull(com.ismartcoding.plain.helpers.AppFileStore.getById(owned.id))
        } finally {
            RustChatFixtures.deleteByIds(chats)
            while (com.ismartcoding.plain.helpers.AppFileStore.getById(owned.id) != null) com.ismartcoding.plain.helpers.AppFileStore.release(owned.id)
            com.ismartcoding.plain.chat.ChatCacher.load()
        }
    }

}

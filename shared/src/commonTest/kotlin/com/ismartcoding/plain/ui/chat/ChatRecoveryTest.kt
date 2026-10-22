package com.ismartcoding.plain.ui.chat

import com.ismartcoding.plain.models.ChatItem
import com.ismartcoding.plain.features.chat.ChatStatus
import com.ismartcoding.plain.features.chat.models.MessageContent
import com.ismartcoding.plain.features.chat.models.MessageText
import com.ismartcoding.plain.features.chat.models.MessageType
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ChatRecoveryTest {
    @BeforeTest
    fun setup() { Dispatchers.setMain(StandardTestDispatcher()) }

    private fun chat(id: String, text: String) = ChatItem(
        id = id, fromId = "peer", toId = "me", status = ChatStatus.SENT,
        content = JsonHelper.jsonEncode(MessageContent(MessageType.TEXT, MessageText(text))),
        createdAt = Instant.parse("2026-01-01T00:00:00Z"), updatedAt = Instant.parse("2026-01-01T00:00:00Z"),
    )
    private fun text() = (ChatViewModel.itemsFlow.value.single().value as MessageText).text

    @AfterTest
    fun cleanup() { ChatViewModel._itemsFlow.value = emptyList(); Dispatchers.resetMain() }

    @Test
    fun recoveryRefreshesOnlyLoadedMessagesAndAppliesCommittedUpdatesAndDeletions() = runTest {
        ChatViewModel.initializeTargetAsync("peer:fixture")
        ChatViewModel._itemsFlow.value = listOf(VChat.from(chat("loaded", "old")), VChat.from(chat("deleted", "old")))
        var calls = 0
        ChatViewModel.refreshLoaded { ids ->
            calls++
            assertEquals(listOf("loaded", "deleted"), ids)
            mapOf("loaded" to chat("loaded", "committed"), "deleted" to null)
        }
        assertEquals(1, calls)
        assertEquals("committed", text())
        ChatViewModel._itemsFlow.value = emptyList()
        ChatViewModel.refreshLoaded { error("Empty view must not fetch") }
    }

    @Test
    fun aNewerLiveUpdateIsPreservedWhenTheRecoveryReadWasAlreadyInFlight() = runTest {
        ChatViewModel.initializeTargetAsync("peer:fixture")
        ChatViewModel._itemsFlow.value = listOf(VChat.from(chat("loaded", "old")))
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val recovery = async {
            ChatViewModel.refreshLoaded { started.complete(Unit); release.await(); mapOf("loaded" to chat("loaded", "snapshot")) }
        }
        started.await()
        ChatViewModel.update(chat("loaded", "newer live event"))
        release.complete(Unit)
        recovery.await()
        assertEquals("newer live event", text())
    }

    @Test
    fun switchingConversationsDiscardsTheOldRecoverySnapshot() = runTest {
        ChatViewModel.initializeTargetAsync("peer:fixture")
        ChatViewModel._itemsFlow.value = listOf(VChat.from(chat("loaded", "old")))
        ChatViewModel.refreshLoaded {
            ChatViewModel.initializeTargetAsync("peer:other")
            ChatViewModel._itemsFlow.value = listOf(VChat.from(chat("other", "current conversation")))
            mapOf("loaded" to chat("loaded", "stale"))
        }
        assertEquals("other", ChatViewModel.itemsFlow.value.single().id)
        assertEquals("current conversation", text())
    }

    @Test
    fun failedRecoveryDoesNotReplaceCurrentMessageState() = runTest {
        ChatViewModel.initializeTargetAsync("peer:fixture")
        ChatViewModel._itemsFlow.value = listOf(VChat.from(chat("loaded", "old")))
        assertFailsWith<IllegalStateException> { ChatViewModel.refreshLoaded { error("read failed") } }
        assertEquals("old", text())
    }
}

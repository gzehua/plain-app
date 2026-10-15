package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.chat.ChatCacher
import com.ismartcoding.plain.chat.RustChatStore
import com.ismartcoding.plain.chat.models.LinkPreview
import com.ismartcoding.plain.chat.models.MessageContent
import com.ismartcoding.plain.chat.models.MessageText
import com.ismartcoding.plain.chat.models.MessageType
import com.ismartcoding.plain.chat.models.toJSONString
import com.ismartcoding.plain.db.*
import com.ismartcoding.plain.enums.ChatStatus
import com.ismartcoding.plain.features.ChatMessageEditor
import com.ismartcoding.plain.graphql.models.ChatItem
import com.ismartcoding.plain.helpers.AppFileStore
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LinkPreviewRustHttpTest {
    @Test
    fun editUsesPersistedMessageAndReleasesPreviewImage() = runBlocking {
        var id = "synthetic-preview-${UUID.randomUUID()}"
        val url = "https://example.com/$id"
        var hash: String? = null
        try {
            val image = AppFileStore.importBytes(id.toByteArray(), "image/png")
            hash = image.id
            val content = MessageContent(MessageType.TEXT, MessageText(url,
                listOf(LinkPreview(url = url, title = id, imageLocalPath = AppFileStore.toFidUri(image))))).toJSONString()
            val value = com.ismartcoding.plain.rust.PlainRust.mainGraphql(
                "mutation(\$content:String!){createChatItem(target: \"peer:local\", content: \$content) { id fromId toId channelId content status statusData createdAt updatedAt }}",
                mapOf("content" to kotlinx.serialization.json.JsonPrimitive(content)),
            ).getValue("createChatItem")
            val chat = com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement<ChatItem>(value)
            id = chat.id
            val stale = ChatItem(id = id, toId = "stale", content = MessageContent(MessageType.TEXT, MessageText("stale")).toJSONString())
            assertTrue(ChatMessageEditor.updateTextAsync(stale, "edited $id"))
            assertEquals("local", stale.toId)
            assertEquals(chat.status, stale.status)
            assertEquals("edited $id", (stale.messageContent.value as MessageText).text)
            assertTrue((stale.messageContent.value as MessageText).linkPreviews.isEmpty())
            assertFalse(AppFileFixtures.exists(image.id))
            assertFalse(ChatMessageEditor.updateTextAsync(stale, "edited $id"))
            assertEquals(stale.content, RustChatStore.getById(id)!!.content)
        } finally {
            com.ismartcoding.plain.chat.ChatManager.deleteOne(id)
            hash?.let { if (AppFileFixtures.exists(it)) AppFileStore.release(it) }
            ChatCacher.load()
        }
    }
}

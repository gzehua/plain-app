package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.features.chat.ChatClient
import com.ismartcoding.plain.features.chat.RustChatStore
import com.ismartcoding.plain.features.chat.ShareSendClient
import com.ismartcoding.plain.features.chat.data.ChatTarget
import com.ismartcoding.plain.features.chat.models.MessageFiles
import com.ismartcoding.plain.features.chat.models.MessageImages
import com.ismartcoding.plain.features.chat.models.MessageShare
import com.ismartcoding.plain.features.chat.models.toJSONString
import com.ismartcoding.plain.db.*
import com.ismartcoding.plain.features.chat.ChatStatus
import com.ismartcoding.plain.features.shares.SharesClient
import com.ismartcoding.plain.api.graphql.models.ChatItem
import com.ismartcoding.plain.features.files.stored.AppFileStore
import com.ismartcoding.plain.rust.request.RustContentApi
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatServiceRustHttpTest {
    @Test
    fun pageShareForwardAndFolderUseNativeGraphqlWithActualOsSources() = runBlocking {
        val marker = "synthetic-chat-service-${UUID.randomUUID()}"
        val local = ChatTarget.parseId("peer:local")
        val root = File(com.ismartcoding.plain.appContext.cacheDir, marker).apply { mkdirs() }
        val source = File(root, "$marker.txt").apply { writeText("$marker actual source") }
        val ids = mutableSetOf<String>()
        val files = mutableSetOf<String>()
        val shares = mutableSetOf<String>()
        val oldService = com.ismartcoding.plain.preferences.UserPrefs.service.value
        suspend fun created(): List<ChatItem> = RustChatStore.getByPeerId("local").filter { it.content.contains(marker) }.also { rows -> ids += rows.map { it.id } }
        try {
            com.ismartcoding.plain.preferences.UserPrefs.service.value = true
            val longText = marker + "😀".repeat(1100)
            val text = ChatClient.sendText(local, longText).also { ids += it.id }
            assertEquals(ChatStatus.SENT, text.status)
            val textFile = (text.messageContent.value as MessageFiles).items.single()
            files += textFile.uri.removePrefix("fid:").substringBefore('.')
            assertEquals(longText.toByteArray().size.toLong(), textFile.size)
            val record = AppFileStore.page(0, 1, "text:${files.single()}").single().appFile
            assertArrayEquals(longText.toByteArray(), File(RustContentApi.directory, record.realPath).readBytes())
            assertTrue(com.ismartcoding.plain.features.chat.sendPickedAttachments(listOf(local, local), listOf(androidx.core.content.FileProvider.getUriForFile(com.ismartcoding.plain.appContext, com.ismartcoding.plain.app.AppIntents.AUTHORITY, source).toString()), null, "$marker caption"))
            val picked = created().filter { (it.messageContent.value as? MessageFiles)?.items?.any { item -> item.fileName == source.name } == true }
            assertEquals(2, picked.size)
            val attachment = (picked.first().messageContent.value as MessageFiles).items.single()
            val hash = attachment.uri.removePrefix("fid:").substringBefore('.')
            files += hash
            assertEquals(source.length(), attachment.size)
            val forward = ChatClient.forward(picked.first().id, listOf(local)).single().also { ids += it.id }
            assertEquals(ChatStatus.SENT, forward.status)
            val encoded = ResourceCrypto.encrypt(attachment.uri)
            val request = BleBinaryFixture.file("local", encoded, 0, 8192)
            val rawReply = com.ismartcoding.plain.platform.network.ble.server.HttpServiceHandler().handleRequest(request, "synthetic BLE MAC")
            val (status, body) = BleBinaryFixture.response(rawReply)
            assertEquals(200, status)
            assertArrayEquals(source.readBytes(), body)
            ChatClient.deleteOne(picked.first().id)
            val page = RustContentApi.query("""chatItems(target: "peer:local", offset: 0, limit: 20, query: "text:$marker") { id }""")["chatItems"]!!.jsonArray
            assertTrue("the sent message must appear in the contract page, got $page", page.any { it.jsonObject["id"]!!.jsonPrimitive.content == forward.id })
            val image = File(root, "$marker.png")
            val bitmap = android.graphics.Bitmap.createBitmap(7, 5, android.graphics.Bitmap.Config.ARGB_8888)
            image.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            val imageUri = androidx.core.content.FileProvider.getUriForFile(com.ismartcoding.plain.appContext, com.ismartcoding.plain.app.AppIntents.AUTHORITY, image).toString()
            assertTrue(com.ismartcoding.plain.features.chat.sendPickedAttachments(listOf(local), listOf(imageUri), null, null))
            val picture = created().mapNotNull { it.messageContent.value as? MessageImages }.single().items.single()
            files += picture.uri.removePrefix("fid:").substringBefore('.')
            assertEquals(7, picture.width)
            assertEquals(5, picture.height)
            assertEquals(image.length(), picture.size)
            assertEquals(1, ShareSendClient.sendFolderShare(listOf(local), root.absolutePath, marker, null).size)
            val card = created().mapNotNull { it.messageContent.value as? MessageShare }.single()
            shares += card.shareId
            assertEquals(2, card.itemCount)
            assertEquals(source.length() + image.length(), card.totalSize)
            assertTrue(SharesClient.getShare(card.shareId)!!.readOnly)
            assertEquals(com.ismartcoding.plain.features.shares.SharedLinkClient.ownLink(card.shareId).token, card.urlToken)
            val staging = File(RustContentApi.directory, "chat-picks")
            assertTrue(staging.listFiles().orEmpty().isEmpty())
        } finally {
            created()
            ChatClient.deleteByIds(ids)
            shares.forEach { SharesClient.deleteShare(it) }
            files.forEach { assertFalse(AppFileFixtures.exists(it)) }
            root.deleteRecursively()
            com.ismartcoding.plain.preferences.UserPrefs.service.value = oldService
            com.ismartcoding.plain.features.chat.ChatCacher.load()
        }
    }
}

package com.ismartcoding.plain.api.graphql

import com.ismartcoding.plain.features.chat.models.*
import com.ismartcoding.plain.features.chat.ChatStatus
import com.ismartcoding.plain.api.graphql.models.*
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Instant

class GraphqlModelDecodeTest {
    private val chat = """{"id":"chat-1","fromId":"me","toId":"peer-1","channelId":null,"content":"{\"type\":\"TEXT\",\"value\":{\"text\":\"hello 中文\"}}","status":"SENT","statusData":"","createdAt":"2026-10-12T00:00:00Z","updatedAt":"2026-10-12T01:00:00Z"}"""

    @Test
    fun chatDecodesTheWireEnvelopeWithoutChangingItsContent() {
        val value = JsonHelper.json.parseToJsonElement(chat)
        val item = JsonHelper.jsonDecodeFromElement<ChatItem>(value)
        assertNull(item.channelId)
        assertEquals(ChatStatus.SENT, item.status)
        assertEquals(Instant.parse("2026-10-12T01:00:00Z"), item.updatedAt)
        assertEquals("hello 中文", (item.messageContent.value as MessageText).text)
        assertEquals(value, JsonHelper.jsonEncodeToElement(item))
    }

    @Test
    fun selectedRequiredFieldsCannotSilentlyBecomeDraftDefaults() {
        assertFailsWith<SerializationException> {
            JsonHelper.jsonDecode<ChatItem>(chat.replace("\"status\":\"SENT\",", ""))
        }
    }

    @Test
    fun nestedConversationAndEditResultsDecodeAsModels() {
        val conversation = JsonHelper.jsonDecode<ChatConversation>("""{"id":"peer-1","chat":$chat}""")
        val edited = JsonHelper.jsonDecode<ChatTextEdit>("""{"changed":true,"chat":$chat}""")
        assertEquals(conversation.chat, edited.chat)
        assertEquals("peer-1", conversation.id)
    }

    @Test
    fun selectedFeedFieldsDecodeWithoutResolverBackedRelationships() {
        val feed = JsonHelper.jsonDecode<Feed>("""{"id":"feed-1","name":"Feed","url":"https://example.org/feed","logo":"/feeds/icon.png","fetchContent":true,"lastSyncAt":null,"lastError":{"code":"","detail":""},"createdAt":"2026-10-12T00:00:00Z","updatedAt":"2026-10-12T00:00:00Z"}""")
        assertEquals("/feeds/icon.png", feed.logo)
        assertNull(feed.lastSyncAt)
        assertEquals(false, feed.hasSyncError)
        assertEquals(0, feed.entryCount)
    }

    @Test
    fun audioDecodesWithoutLoadingTags() {
        val audio = JsonHelper.jsonDecode<Audio>("""{"id":"audio-1","title":"Track","artist":"Artist","path":"/track.mp3","durationMs":1000,"size":2000,"bucketId":"bucket-1","albumFileId":"","createdAt":"2026-10-12T00:00:00Z","updatedAt":"2026-10-12T00:00:00Z","isFavorite":false}""")
        assertEquals("audio-1", audio.id)
        assertEquals(false, JsonHelper.jsonEncodeToElement(audio).toString().contains("\"tags\""))
    }

    @Test
    fun playlistItemsDecodeMediaIdsAndPaths() {
        val item = JsonHelper.jsonDecode<AudioItem>("""{"mediaId":"audio-1","path":"/track.mp3","title":"Track","artist":"Artist","durationMs":1000}""")
        assertEquals("audio-1", item.mediaId)
        assertEquals("/track.mp3", item.path)
        assertEquals(1000L, item.durationMs)
    }

    @Test
    fun allMediaModelsImplementTheSharedFields() {
        listOf(Audio::class.java, Image::class.java, Video::class.java, Doc::class.java).forEach { type ->
            assertEquals(true, MediaItem::class.java.isAssignableFrom(type), type.simpleName)
        }
    }

    @Test
    fun hostModelsPreserveLongScalarsAndSchemaFieldNames() {
        val relation = JsonHelper.jsonDecode<TagHostRelation>("""{"tagId":"tag-1","key":"file-1","type":1,"title":"file","sizeBytes":4294967296,"createdAt":"2026-10-12T00:00:00Z"}""")
        assertEquals(4294967296L, relation.sizeBytes)
        val history = JsonHelper.jsonDecode<AudioPlayHistory>("""{"mediaId":"audio-1","path":"/music.mp3","title":"Music","artist":"Artist","durationMs":1234,"playCount":2147483647,"playedAt":"2026-10-12T00:00:00Z"}""")
        assertEquals(2147483647, history.playCount)
    }

    @Test
    fun messageContentUsesTheSharedJsonHelperForEveryEnvelopeType() {
        val messages = listOf(
            MessageContent(MessageType.TEXT, MessageText("hello")),
            MessageContent(MessageType.IMAGES, MessageImages(listOf(MessageFile(uri = "fid:image", size = 3)))),
            MessageContent(MessageType.FILES, MessageFiles(listOf(MessageFile(uri = "fsid:file", size = 5, durationMs = 30000)))),
            MessageContent(MessageType.SHARE, MessageShare("share-1", "secret", SharePeerInfo("peer", "127.0.0.1", 8080), "Folder")),
        )
        messages.forEach { message ->
            val encoded = JsonHelper.jsonEncode(message)
            val decoded = JsonHelper.jsonDecode<MessageContent>(encoded)
            assertEquals(message.type, decoded.type)
            assertEquals(JsonHelper.json.parseToJsonElement(encoded), JsonHelper.jsonEncodeToElement(decoded))
        }
    }

    @Test
    fun mediaInfoUnionUsesTheGraphqlTypename() {
        val info = JsonHelper.jsonDecode<MediaFileInfo>("""{"__typename":"VideoFileInfo","width":1920,"height":1080,"durationMs":30000,"location":null}""") as VideoFileInfo
        assertEquals(30000L, info.durationMs)
    }
    @Test
    fun nativePeerRecordsDecodeSnakeCaseFieldsAndKeepDerivedAddress() {
        val peer = JsonHelper.jsonDecode<PeerRecord>("""{"id":"peer-1","name":"Peer","ip":"127.0.0.1","key":"key","public_key":"public","status":"PAIRED","port":8080,"device_type":"PHONE","token":"","created_at":"2026-10-12T00:00:00Z","updated_at":"2026-10-12T00:00:00Z","address":{"bestIp":"127.0.0.1","name":"Peer","baseUrl":"https://127.0.0.1:8080","apiUrl":"https://127.0.0.1:8080/peer_graphql","statusWsUrl":"wss://127.0.0.1:8080/peer_status"}}""")
        assertEquals("public", peer.publicKey)
        assertEquals("127.0.0.1", peer.address?.bestIp)
        assertEquals(peer, JsonHelper.jsonDecodeFromElement<PeerRecord>(JsonHelper.jsonEncodeToElement(peer)))
    }

    @Test
    fun nativeChannelMembersRemainAJsonStringAndUseThePublicMemberModel() {
        val channel = JsonHelper.jsonDecode<ChatChannelRecord>("""{"id":"channel-1","name":"Channel","key":"key","owner_id":"me","members":"[{\"peerId\":\"peer-1\",\"status\":\"JOINED\"}]","version":4294967296,"status":"JOINED","created_at":"2026-10-12T00:00:00Z","updated_at":"2026-10-12T00:00:00Z"}""")
        assertEquals(4294967296L, channel.version)
        assertEquals(listOf("peer-1"), channel.memberIds())
        assertEquals(channel, JsonHelper.jsonDecodeFromElement<ChatChannelRecord>(JsonHelper.jsonEncodeToElement(channel)))
    }

    @Test
    fun nativeRecordDecodersKeepRejectingIncompleteSnapshots() {
        assertFailsWith<SerializationException> { JsonHelper.jsonDecode<PeerRecord>("""{"id":"peer-1"}""") }
        assertFailsWith<SerializationException> { JsonHelper.jsonDecode<ChatChannelRecord>("""{"id":"channel-1"}""") }
    }

}

package com.ismartcoding.plain.features.sms

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.DSmsCounts
import kotlin.time.Instant
import kotlinx.serialization.json.*

object RustSmsQuery {
    private const val MESSAGE_FIELDS = "id body address date: sentAt serviceCenter read threadId type: typeCode subscriptionId isMms attachments { path contentType name }"
    private const val CONVERSATION_FIELDS = "id address snippet date: lastMessageAt messageCount read addresses"

    suspend fun messages(query: String, offset: Int, limit: Int): List<DMessage> =
        search(query, limit, offset)

    suspend fun search(query: String, limit: Int, offset: Int, includeTrashed: Boolean = false): List<DMessage> =
        JsonHelper.jsonDecodeFromElement(PlainRust.mainQuery("sms(query: ${gql(query)}, offset: $offset, limit: $limit, includeTrashed: $includeTrashed) { $MESSAGE_FIELDS }").getValue("sms"))

    suspend fun ids(query: String, includeTrashed: Boolean = false): Set<String> =
        PlainRust.mainQuery("smsIds(query: ${gql(query)}, includeTrashed: $includeTrashed)")
            .getValue("smsIds").jsonArray.map { it.jsonPrimitive.content }.toSet()

    suspend fun textIds(filters: List<String>): Set<String> =
        PlainRust.mainQuery("mmsTextIds(filters: [${filters.joinToString(",", transform = ::gql)}])")
            .getValue("mmsTextIds").jsonArray.map { it.jsonPrimitive.content }.toSet()

    suspend fun count(query: String): Int =
        PlainRust.mainQuery("smsCount(query: ${gql(query)})").getValue("smsCount").jsonPrimitive.int

    suspend fun conversations(query: String, offset: Int, limit: Int): List<DMessageConversation> =
        JsonHelper.jsonDecodeFromElement(PlainRust.mainQuery("smsConversations(query: ${gql(query)}, offset: $offset, limit: $limit) { $CONVERSATION_FIELDS }").getValue("smsConversations"))

    suspend fun conversationCount(query: String): Int =
        PlainRust.mainQuery("smsConversationCount(query: ${gql(query)})").getValue("smsConversationCount").jsonPrimitive.int

    suspend fun archivedConversations(query: String = "", offset: Int = 0, limit: Int = Int.MAX_VALUE): List<DMessageConversation> =
        JsonHelper.jsonDecodeFromElement(PlainRust.mainQuery("archivedSmsConversations(query: ${gql(query)}, offset: $offset, limit: $limit) { $CONVERSATION_FIELDS }").getValue("archivedSmsConversations"))

    suspend fun conversationDate(id: String): Instant? =
        PlainRust.mainQuery("smsConversationDate(id: ${gql(id)})").getValue("smsConversationDate").let {
            if (it is JsonNull) null else Instant.parse(it.jsonPrimitive.content)
        }

    suspend fun conversationDateEpochMillis(id: String): Long? = conversationDate(id)?.toEpochMilliseconds()

    suspend fun counts(): JsonObject = PlainRust.mainQuery("smsBoxCounts { total inbox sent drafts }").getValue("smsBoxCounts").jsonObject

    suspend fun countModel(): DSmsCounts {
        val result = counts()
        return DSmsCounts(result.getValue("total").jsonPrimitive.int, result.getValue("inbox").jsonPrimitive.int,
            result.getValue("sent").jsonPrimitive.int, result.getValue("drafts").jsonPrimitive.int)
    }
}

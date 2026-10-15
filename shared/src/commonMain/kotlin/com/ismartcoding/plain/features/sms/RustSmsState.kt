package com.ismartcoding.plain.features.sms

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.db.DArchivedConversation
import kotlin.time.Instant
import kotlinx.serialization.json.*

object RustSmsState {
    suspend fun archives(): List<DArchivedConversation> =
        PlainRust.mainQuery("smsArchiveRecords { conversationId archivedAt }").getValue("smsArchiveRecords").jsonArray.map {
            val row = it.jsonObject
            DArchivedConversation(row.getValue("conversationId").jsonPrimitive.content,
                Instant.parse(row.getValue("archivedAt").jsonPrimitive.content))
        }

    suspend fun archive(id: String, date: Instant) {
        PlainRust.mainMutate("archiveSmsConversation(id: ${gql(id)}, date: ${gql(date.toString())})")
    }
    suspend fun unarchive(id: String) { PlainRust.mainMutate("unarchiveSmsConversation(id: ${gql(id)})") }
    suspend fun trashed(): Set<String> = PlainRust.mainQuery("trashedSmsIds").getValue("trashedSmsIds").jsonArray.map { it.jsonPrimitive.content }.toSet()
}

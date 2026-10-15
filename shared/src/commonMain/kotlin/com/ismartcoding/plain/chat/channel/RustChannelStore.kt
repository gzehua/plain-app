package com.ismartcoding.plain.chat.channel

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.chat.models.ChatChannelRecord
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

object RustChannelStore {
    suspend fun getById(id: String): ChatChannelRecord? {
        val value=PlainRust.mainQuery("chatChannelRecord(id: ${gql(id)}) { id name key owner_id: ownerId members { peerId status } version status created_at: createdAt updated_at: updatedAt }").getValue("chatChannelRecord")
        if (value is JsonNull) return null
        val row=value.jsonObject
        return JsonHelper.jsonDecodeFromElement(JsonObject(row + ("members" to JsonPrimitive(row.getValue("members").toString()))))
    }
}

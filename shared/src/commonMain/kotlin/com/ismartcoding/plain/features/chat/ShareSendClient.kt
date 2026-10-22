package com.ismartcoding.plain.features.chat

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.models.ChatItem
import com.ismartcoding.plain.features.chat.data.ChatTarget
import com.ismartcoding.plain.features.chat.models.MessageContent
import com.ismartcoding.plain.features.chat.models.toJSONString
import com.ismartcoding.plain.rust.graphql.gql
import com.ismartcoding.plain.rust.graphql.gqlIds
import kotlinx.serialization.json.*
import kotlin.time.Instant

object ShareSendClient {
    suspend fun sendContent(targets: List<ChatTarget>, content: MessageContent): List<ChatItem> =
        send("sendChatItems", "targets: ${gqlIds(targets.map { it.encodedToId })}, content: ${gql(content.toJSONString())}")

    suspend fun sendShare(targets: List<ChatTarget>, shareId: String): List<ChatItem> =
        send("sendShare", "targets: ${gqlIds(targets.map { it.encodedToId })}, shareId: ${gql(shareId)}")

    suspend fun sendFolderShare(targets: List<ChatTarget>, dirPath: String, name: String, expiresAt: Instant?): List<ChatItem> =
        send("sendFolderShare", "targets: ${gqlIds(targets.map { it.encodedToId })}, path: ${gql(dirPath)}, name: ${gql(name)}, expiresAt: ${expiresAt?.toString()?.let(::gql) ?: "null"}")

    private suspend fun send(field: String, args: String): List<ChatItem> =
        RustGraphqlApi.mutate("$field($args) { ${RustChatStore.FIELDS} }")
            .getValue(field).jsonArray.map(RustChatStore::decodeGraphql)
}

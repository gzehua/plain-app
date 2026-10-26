package com.ismartcoding.plain.features.sessions

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.rust.native.RustNativeApi
import com.ismartcoding.plain.ui.sessions.VSession
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.boolean
import kotlin.time.Instant

object RustSessionStore {
    suspend fun list(): List<VSession> = JsonHelper.jsonDecodeFromElement<List<SessionFacts>>(
        RustGraphqlApi.graphql(Query.sessions()).getValue("sessions")
    ).map {
        VSession(it.clientId, it.name, SessionType.valueOf(it.type), it.token, it.clientIp, it.osName, it.osVersion,
            it.browserName, it.browserVersion, Instant.parse(it.createdAt), Instant.parse(it.updatedAt),
            it.lastSeenAt?.let(Instant::parse))
    }
    suspend fun create(name: String): CreatedSession = RustNativeApi.createSession(name)
    suspend fun rename(clientId: String, name: String): Boolean =
        RustGraphqlApi.graphql(Mutation.renameSession(clientId, name)).getValue("renameSession").jsonPrimitive.boolean
    suspend fun delete(clientId: String) { RustGraphqlApi.graphql(Mutation.revokeSession(clientId)) }
}

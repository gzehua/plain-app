package com.ismartcoding.plain.features.sessions

import com.ismartcoding.plain.rust.request.NativeManagementClient

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.ui.sessions.VSession
import com.ismartcoding.plain.features.sessions.SessionType
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.boolean
import kotlin.time.Instant

object RustSessionStore {
    suspend fun list(): List<VSession> = JsonHelper.jsonDecodeFromElement<List<SessionFacts>>(
        PlainRust.mainQuery("sessions { clientId clientName type token clientIp osName osVersion browserName browserVersion createdAt updatedAt lastSeenAt }").getValue("sessions")
    ).map {
        VSession(it.clientId, it.name, SessionType.valueOf(it.type), it.token, it.clientIp, it.osName, it.osVersion,
            it.browserName, it.browserVersion, Instant.parse(it.createdAt), Instant.parse(it.updatedAt),
            it.lastSeenAt?.let(Instant::parse))
    }
    suspend fun create(name: String): CreatedSession = NativeManagementClient.createSession(name)
    suspend fun rename(clientId: String, name: String): Boolean =
        PlainRust.mainMutate("renameSession(clientId: ${gql(clientId)}, name: ${gql(name)})").getValue("renameSession").jsonPrimitive.boolean
    suspend fun delete(clientId: String) { PlainRust.mainMutate("revokeSession(clientId: ${gql(clientId)})") }
}

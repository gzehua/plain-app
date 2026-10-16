package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.rust.request.ContentApiSession
import com.ismartcoding.plain.rust.request.RustContentApi
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.jsonObject

/**
 * System settings: the credentials, tokens and identity material Rust owns.
 *
 * These stay on GraphQL because they are low-traffic, must be reachable by
 * remote clients through the same schema, and are written as commands rather
 * than field assignments — unlike [UserPrefs], which writes straight through
 * the native bridge.
 */
class SystemPrefsClient(private val session: ContentApiSession? = null) {
    private var systemProjection: MutableStateFlow<SystemState>? = null
    private val mutex = Mutex()
    val system: StateFlow<SystemState> get() = checkNotNull(systemProjection).asStateFlow()

    suspend fun refresh() = mutex.withLock { refreshLocked() }

    private suspend fun refreshLocked() {
        val result = RustContentApi.graphql("query { $SELECTION }", session = session)
        apply(result)
    }

    private fun apply(result: kotlinx.serialization.json.JsonObject) {
        val system = JsonHelper.jsonDecodeFromElement<SystemState>(result.getValue("systemSettings"))
        systemProjection?.let { it.value = system } ?: run { systemProjection = MutableStateFlow(system) }
    }

    suspend fun updateSystem(command: SystemCommand) = mutex.withLock {
        val result = RustContentApi.graphql("mutation(\$input: SystemSettingsInput!) { updateSystemSettings(input: \$input) { $SELECTION } }",
            mapOf("input" to JsonHelper.jsonEncodeToElement<SystemCommand>(command)), session)
        apply(result.getValue("updateSystemSettings").jsonObject)
    }

    suspend fun verifyAdbToken(token: String): Boolean = JsonHelper.jsonDecodeFromElement(
        RustContentApi.graphql("query(\$token: String!) { verifyAdbToken(token: \$token) }", mapOf("token" to JsonHelper.jsonEncodeToElement(token)), session).getValue("verifyAdbToken"))

    companion object {
        private const val SELECTION = "systemSettings { password passwordType authTwoFactor rotateUrlTokenOnRestart adbToken updateInfo appUpdateAvailable urlToken apiPermissions onboardingCompleted clientId mdnsHostname signaturePublicKey }"
        val local = SystemPrefsClient()
    }
}
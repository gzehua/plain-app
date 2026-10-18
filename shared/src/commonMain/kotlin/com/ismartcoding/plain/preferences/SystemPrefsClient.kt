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

    suspend fun set(key: String, value: kotlinx.serialization.json.JsonElement) = mutex.withLock {
        RustContentApi.graphql(
            "mutation(\$key: String!, \$value: JSON!) { setSystemPref(key: \$key, value: \$value) { affectedCount } }",
            mapOf("key" to JsonHelper.jsonEncodeToElement(key), "value" to value), session,
        )
        refreshLocked()
    }

    suspend fun resetPassword() = reset("resetPassword")
    suspend fun resetUrlToken() = reset("resetUrlToken")
    suspend fun resetAdbToken() = reset("resetAdbToken")

    private suspend fun reset(field: String) = mutex.withLock {
        RustContentApi.graphql("mutation { $field }", session = session)
        refreshLocked()
    }

    suspend fun verifyAdbToken(token: String): Boolean = JsonHelper.jsonDecodeFromElement(
        RustContentApi.graphql("query(\$token: String!) { verifyAdbToken(token: \$token) }", mapOf("token" to JsonHelper.jsonEncodeToElement(token)), session).getValue("verifyAdbToken"))

    companion object {
        private const val SELECTION = "systemSettings { password passwordType authTwoFactor rotateUrlTokenOnRestart adbToken updateInfo appUpdateAvailable urlToken apiPermissions onboardingCompleted clientId mdnsHostname signaturePublicKey }"
        val local = SystemPrefsClient()
    }
}
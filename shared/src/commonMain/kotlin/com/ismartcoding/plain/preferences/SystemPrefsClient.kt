package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi


import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SystemPrefsClient {
    private var systemProjection: MutableStateFlow<SystemSettings>? = null
    private val mutex = Mutex()
    val system: StateFlow<SystemSettings> get() = checkNotNull(systemProjection).asStateFlow()

    suspend fun refresh() = mutex.withLock { refreshLocked() }

    private suspend fun refreshLocked() {
        val system = com.ismartcoding.plain.rust.native.RustNativeApi.systemSettings()
        systemProjection?.let { it.value = system } ?: run { systemProjection = MutableStateFlow(system) }
    }

    suspend fun set(key: String, value: kotlinx.serialization.json.JsonElement) = mutex.withLock {
        RustGraphqlApi.graphql(
            "mutation(\$key: String!, \$value: JSON!) { setSystemPref(key: \$key, value: \$value) { affectedCount } }",
            mapOf("key" to JsonHelper.jsonEncodeToElement(key), "value" to value),
        )
        refreshLocked()
    }

    suspend fun resetPassword() = reset("resetPassword")
    suspend fun resetUrlToken() = reset("resetUrlToken")
    suspend fun resetAdbToken() = reset("resetAdbToken")

    private suspend fun reset(field: String) = mutex.withLock {
        RustGraphqlApi.graphql("mutation { $field }")
        refreshLocked()
    }

    companion object {
        val local = SystemPrefsClient()
    }
}
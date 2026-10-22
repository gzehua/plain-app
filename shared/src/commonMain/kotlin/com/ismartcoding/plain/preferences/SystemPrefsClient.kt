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

    suspend fun refresh() = mutex.withLock {
        val previous = systemProjection?.value
        val system = com.ismartcoding.plain.rust.native.RustNativeApi.systemSettings()
        systemProjection?.let { it.value = system } ?: run { systemProjection = MutableStateFlow(system) }
        if (previous != null && previous.apiPermissions != system.apiPermissions) {
            com.ismartcoding.plain.lib.sendEvent(com.ismartcoding.plain.app.events.HostEvent(
                com.ismartcoding.plain.app.events.EventType.PERMISSIONS_UPDATED,
                JsonHelper.jsonEncode(com.ismartcoding.plain.features.permissions.getGrantedWebPermissionsAsync()),
            ))
        }
    }

    suspend fun set(key: String, value: kotlinx.serialization.json.JsonElement) {
        RustGraphqlApi.graphql(
            "mutation(\$key: String!, \$value: JSON!) { setSystemPref(key: \$key, value: \$value) { affectedCount } }",
            mapOf("key" to JsonHelper.jsonEncodeToElement(key), "value" to value),
        )
    }

    suspend fun resetPassword() = reset("resetPassword")
    suspend fun resetUrlToken() = reset("resetUrlToken")
    suspend fun resetAdbToken() = reset("resetAdbToken")

    private suspend fun reset(field: String) {
        RustGraphqlApi.graphql("mutation { $field }")
    }

    companion object {
        val local = SystemPrefsClient()
    }
}
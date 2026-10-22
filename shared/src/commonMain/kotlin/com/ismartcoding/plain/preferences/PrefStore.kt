package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.system.PlatformLock
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.rust.native.RustNativeApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

internal object PrefStore {
    private val lock = PlatformLock()
    private val refreshLock = Mutex()
    private val flows = mutableListOf<Pref<*>>()

    private var snapshot: JsonObject? = null

    fun <T> register(flow: Pref<T>): Pref<T> = lock.withLock {
        snapshot?.get(flow.key)?.let { flow.restore(flow.decode(it, flow.value)) }
        flows.add(flow)
        flow
    }

    suspend fun refresh() = refreshLock.withLock {
        val stored = RustNativeApi.prefsSnapshot()
        SecuritySettingsState.apply(stored)
        val previous = lock.withLock {
            val previous = snapshot
            snapshot = stored
            flows.forEach { it.restore(it.decode(stored[it.key], it.value)) }
            previous
        }
        previous?.let { PrefsEffects.applyChanges(it, stored) }
    }

    fun write(key: String, value: JsonElement) {
        runBlocking(Dispatchers.IO) {
            RustGraphqlApi.graphql(
                "mutation(\$key: String!, \$value: JSON!) { setPref(key: \$key, value: \$value) }",
                mapOf("key" to JsonHelper.jsonEncodeToElement(prefKey(key)), "value" to value),
            )
        }
    }

}

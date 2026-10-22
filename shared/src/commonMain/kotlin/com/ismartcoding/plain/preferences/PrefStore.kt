package com.ismartcoding.plain.preferences

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.rust.native.RustNativeApi

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.system.PlatformLock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Owns the preferences snapshot and the write path behind every [Pref].
 *
 * Keeping the snapshot here means the App mirrors the store without ever
 * owning it: Rust supplies the defaults and validates the writes, the flows
 * only cache what Rust already committed, and a remote change arrives through
 * [refresh] and lands in the same place a local write would.
 */
internal object PrefStore {
    private val lock = PlatformLock()
    private val refreshLock = Mutex()
    private val flows = mutableListOf<Pref<*>>()

    /** Null until the first load; that first read seeds the flows without
     *  firing platform effects, because startup already applies them. */
    private var snapshot: JsonObject? = null

    /** Registers a flow and seeds it from the snapshot when one is already loaded. */
    fun <T> register(flow: Pref<T>): Pref<T> = lock.withLock {
        snapshot?.get(flow.key)?.let { flow.restore(flow.decode(it, flow.value)) }
        flows.add(flow)
        flow
    }

    /** Reads the authoritative snapshot and publishes it to every flow. */
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

    /** Submit only; the Host notification publishes the committed snapshot. */
    fun write(key: String, value: JsonElement) {
        runBlocking(Dispatchers.IO) {
            RustGraphqlApi.graphql(
                "mutation(\$key: String!, \$value: JSON!) { setPref(key: \$key, value: \$value) }",
                mapOf("key" to JsonHelper.jsonEncodeToElement(prefKey(key)), "value" to value),
            )
        }
    }

    suspend fun runAction(action: PrefAction) {
        RustGraphqlApi.mutate(action.selection())
    }
}

package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.system.PlatformLock
import com.ismartcoding.plain.platform.IODispatcher
import com.ismartcoding.plain.rust.request.RustContentApi
import kotlinx.coroutines.runBlocking
import com.ismartcoding.plain.rust.bridge.RustPrefsBridge
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Owns the user-settings snapshot and the write path behind every [Pref].
 *
 * Keeping the snapshot here means the App mirrors the store without ever
 * owning it: Rust supplies the defaults and validates the writes, the flows
 * only cache what Rust already committed, and a remote change arrives through
 * [refresh] and lands in the same place a local write would.
 */
internal object PrefStore {
    private val lock = PlatformLock()
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
    fun load() = refresh()

    fun refresh() {
        val stored = JsonHelper.jsonDecode<JsonObject>(RustPrefsBridge.userSnapshot())
        val previous = lock.withLock {
            val previous = snapshot
            snapshot = stored
            flows.forEach { it.restore(it.decode(stored[it.key], it.value)) }
            previous
        }
        previous?.let { UserPrefsEffects.applyChanges(it, stored) }
    }

    /**
     * Persists one field through Rust, then folds it into the cached snapshot.
     *
     * The native call happens under the lock so concurrent writes to different
     * fields cannot interleave; the platform effects derived from the change run
     * after it, outside the lock, because they start services and are slow.
     */
    fun write(key: String, value: JsonElement) {
        val (previous, current) = lock.withLock {
            val previous = snapshot ?: JsonObject(emptyMap())
            runBlocking(IODispatcher) {
                RustContentApi.graphql(
                    "mutation(\$key: String!, \$value: JSON!) { setUserPref(key: \$key, value: \$value) }",
                    mapOf("key" to JsonHelper.jsonEncodeToElement(userPreferenceKey(key)), "value" to value),
                )
            }
            val current = JsonObject(previous + (key to value))
            snapshot = current
            previous to current
        }
        UserPrefsEffects.applyChanges(previous, current)
    }

    /**
     * Runs a Rust-side command, then re-reads the snapshot so the flows pick up
     * whatever the command rewrote — an action may touch fields the App never
     * named.
     */
    suspend fun runAction(action: UserAction) {
        RustContentApi.mutate(action.selection())
        refresh()
    }
}
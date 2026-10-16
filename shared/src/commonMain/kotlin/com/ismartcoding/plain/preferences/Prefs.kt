package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.IODispatcher
import com.ismartcoding.plain.rust.bridge.RustPrefsBridge
import com.ismartcoding.plain.rust.request.RustContentApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

object Prefs {
    fun load() {
        RustPrefsBridge.open(RustContentApi.directory, RustContentApi.cacheDirectory)
        PrefStore.load()
    }

    /**
     * Re-reads user and system settings from Rust. Startup, a local write
     * echoed back as `PREFS_UPDATED`, and a change made by a remote client all
     * converge here, so the App never has to tell those cases apart.
     */
    suspend fun refresh() {
        PrefStore.refresh()
        SystemPrefsClient.local.refresh()
    }

    val systemSnapshot: Map<String, JsonElement> get() = runBlocking(IODispatcher) {
        RustContentApi.query("systemPrefs").getValue("systemPrefs").jsonObject
    }
    fun systemString(key: String): String? = systemSnapshot[key]?.let { JsonHelper.jsonDecodeFromElement(it) }
    fun setSystemString(key: String, value: String) = runBlocking(IODispatcher) {
        RustContentApi.graphql("mutation(\$key: String!, \$value: JSON!) { setSystemPref(key: \$key, value: \$value) { affectedCount } }",
            mapOf("key" to JsonHelper.jsonEncodeToElement(key), "value" to JsonHelper.jsonEncodeToElement(value)))
    }
    fun removeSystemPref(key: String) = runBlocking(IODispatcher) {
        RustContentApi.graphql("mutation(\$key: String!) { removeSystemPref(key: \$key) { affectedCount } }", mapOf("key" to JsonHelper.jsonEncodeToElement(key)))
    }
}
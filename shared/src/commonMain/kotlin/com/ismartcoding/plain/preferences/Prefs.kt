package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.rust.bridge.RustPrefsBridge
import com.ismartcoding.plain.rust.request.RustContentApi

object Prefs {
    fun load() {
        RustPrefsBridge.open(RustContentApi.directory, RustContentApi.cacheDirectory)
        RustContentApi.start()
        PrefStore.load()
    }

    suspend fun refresh() {
        PrefStore.refresh()
        SystemPrefsClient.local.refresh()
    }
}

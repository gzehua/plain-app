package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.rust.bridge.RustPrefsBridge
import com.ismartcoding.plain.rust.request.RustRuntime

object Prefs {
    fun load() {
        RustPrefsBridge.open(RustRuntime.dataDir, RustRuntime.cacheDir)
        RustRuntime.start()
        PrefStore.load()
    }

    suspend fun refresh() {
        PrefStore.refresh()
        SystemPrefsClient.local.refresh()
    }
}

package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.rust.native.RustPrefsBridge
import com.ismartcoding.plain.rust.RustRuntime

object Prefs {
    fun load() {
        RustPrefsBridge.open(RustRuntime.dataDir, RustRuntime.cacheDir)
        RustRuntime.start()
    }

    suspend fun refresh() {
        PrefStore.refresh()
        SystemPrefsClient.local.refresh()
    }
}

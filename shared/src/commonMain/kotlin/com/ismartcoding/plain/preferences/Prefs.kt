package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.rust.native.RustPrefsBridge
import com.ismartcoding.plain.rust.RustRuntime
import com.ismartcoding.plain.platform.system.cacheDirPath

object Prefs {
    fun load() {
        RustPrefsBridge.open(RustRuntime.dataDir, cacheDirPath())
        RustRuntime.start()
    }

    suspend fun refresh() {
        PrefStore.refresh()
        SystemPrefsClient.local.refresh()
    }
}

package com.ismartcoding.plain.preferences

object Prefs {
    suspend fun refresh() {
        PrefStore.refresh()
        SystemPrefsClient.local.refresh()
    }
}

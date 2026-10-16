package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.lib.logcat.LogCat
import kotlinx.coroutines.*

internal suspend fun discoverySession(token: String, acquire: suspend (String) -> Unit, release: suspend (String) -> Unit) {
    try {
        acquire(token)
        awaitCancellation()
    } finally {
        withContext(NonCancellable) {
            try { release(token) }
            catch (error: Exception) { LogCat.e("Discovery session release failed", error) }
        }
    }
}

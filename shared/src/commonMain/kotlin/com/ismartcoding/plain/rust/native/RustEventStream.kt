package com.ismartcoding.plain.rust.native

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

internal suspend fun readRustStream(
    next: suspend () -> String,
    consume: suspend (String) -> Unit,
    closed: () -> Unit,
) {
    try {
        while (currentCoroutineContext().isActive) {
            val value = next()
            if (value.isNotEmpty()) consume(value)
        }
    } finally {
        closed()
    }
}

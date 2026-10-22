package com.ismartcoding.plain.rust.native

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

// The two blocking pollers must not use the parallelism reserved for requests.
private val eventReads = Dispatchers.IO.limitedParallelism(2)

internal suspend fun readRustStream(
    next: suspend () -> String,
    consume: suspend (String) -> Unit,
    closed: () -> Unit,
) {
    try {
        while (currentCoroutineContext().isActive) {
            val value = withContext(eventReads) { next() }
            if (value.isNotEmpty()) consume(value)
        }
    } finally {
        closed()
    }
}

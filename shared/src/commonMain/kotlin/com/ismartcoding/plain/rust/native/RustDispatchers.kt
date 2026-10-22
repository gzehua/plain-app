package com.ismartcoding.plain.rust.native

import kotlinx.coroutines.CoroutineDispatcher

internal expect object RustDispatchers {
    val requests: CoroutineDispatcher
    val events: CoroutineDispatcher
}

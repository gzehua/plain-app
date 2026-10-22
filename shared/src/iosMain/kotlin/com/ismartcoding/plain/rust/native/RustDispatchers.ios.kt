package com.ismartcoding.plain.rust.native

import kotlinx.coroutines.*
import kotlinx.cinterop.ExperimentalForeignApi
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import kotlin.coroutines.CoroutineContext

@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class, ExperimentalForeignApi::class)
internal actual object RustDispatchers {
    actual val requests: CoroutineDispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            dispatch_async(dispatch_get_global_queue(0, 0uL)) { block.run() }
        }
    }
    actual val events: CoroutineDispatcher = newFixedThreadPoolContext(2, "Rust native events")
}

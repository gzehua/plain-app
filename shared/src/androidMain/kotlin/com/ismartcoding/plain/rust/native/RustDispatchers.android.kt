package com.ismartcoding.plain.rust.native

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual object RustDispatchers {
    actual val requests: CoroutineDispatcher = Dispatchers.IO
    actual val events: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(2)
}

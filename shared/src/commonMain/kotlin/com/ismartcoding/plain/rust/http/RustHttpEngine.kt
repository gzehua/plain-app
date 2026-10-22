package com.ismartcoding.plain.rust.http

import com.ismartcoding.plain.rust.request.RustRuntime
import com.ismartcoding.plain.lib.withIO

internal object RustHttpEngine {
    val isRunning: Boolean get() = RustRuntime.httpGeneration != 0L
    suspend fun start(): Unit = withIO { RustRuntime.start() }
    suspend fun stop(): Unit = withIO { RustRuntime.stopHttp() }
    fun failed(serverGeneration: Long): Boolean = RustRuntime.httpFailed(serverGeneration)
}

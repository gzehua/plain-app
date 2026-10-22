package com.ismartcoding.plain.rust

import com.ismartcoding.plain.lib.JsonHelper

import com.ismartcoding.plain.platform.system.*
import com.ismartcoding.plain.rust.native.RustNativeApi
import com.ismartcoding.plain.rust.host.RustHostApiHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlin.concurrent.Volatile

object RustRuntime {
    private val lock = PlatformLock()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile var isCoreInitialized: Boolean = false
        private set

    val dataDir: String by lazy { dataDirPath() }
    @Volatile private var logPath: String? = null
    val logFile: String get() = checkNotNull(logPath) { "Rust log path is not initialized" }
    @Volatile private var imageCachePath: String? = null
    @Volatile private var videoCachePath: String? = null
    @Volatile private var sharedPreviewPath: String? = null
    val cacheDir: String by lazy { cacheDirPath() }
    val videoCacheDir: String get() = checkNotNull(videoCachePath) { "Rust video cache is not initialized" }
    val imageCacheDir: String get() = checkNotNull(imageCachePath) { "Rust image cache is not initialized" }
    val sharedPreviewDir: String get() = checkNotNull(sharedPreviewPath) { "Rust shared preview cache is not initialized" }

    private var sessionToken: String? = null
    private var collectorsStarted = false

    private val startupConfig by lazy {
        RustHttpServerConfig(isDebugBuild(), startupSystemProperties())
    }

    fun start() = lock.withLock {
        val token = sessionToken ?: generateChaCha20Key().also { sessionToken = it }
        val config = startupConfig
        val result = JsonHelper.jsonDecode<RustHttpServerResult>(RustNativeApi.start(
            dataDir, token, JsonHelper.jsonEncode(config)))
        check(result.errorCode.isEmpty()) { result.error }
        require(result.cacheDir.startsWith('/') && result.imageCacheDir.startsWith('/') && result.videoCacheDir.startsWith('/')) { "Rust cache paths required" }
        check(result.cacheDir == cacheDir) { "Rust cache root differs from startup configuration" }
        require(result.logFile.startsWith('/')) { "Rust log file path required" }
        require(result.sharedPreviewDir.startsWith('/')) { "Rust shared preview cache path required" }
        logPath = result.logFile
        imageCachePath = result.imageCacheDir
        videoCachePath = result.videoCacheDir
        sharedPreviewPath = result.sharedPreviewDir
        kotlinx.coroutines.runBlocking(com.ismartcoding.plain.platform.IODispatcher) { com.ismartcoding.plain.preferences.RustSystemState.refresh() }
        isCoreInitialized = true
        if (!collectorsStarted) {
            RustEventHandler.start(scope)
            RustHostApiHandler.start(scope)
            collectorsStarted = true
        }
    }
}

package com.ismartcoding.plain.rust

import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.lib.JsonHelper

import com.ismartcoding.plain.platform.system.*
import com.ismartcoding.plain.rust.graphql.ApiSession
import com.ismartcoding.plain.rust.native.RustNativeApi
import com.ismartcoding.plain.rust.event.RustEventHandler
import com.ismartcoding.plain.rust.host.RustHostApiHandler
import com.ismartcoding.plain.lib.withIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.*
import kotlin.concurrent.Volatile

object RustRuntime {
    private val lock = PlatformLock()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile private var localSession: ApiSession? = null

    internal fun startedSession(): ApiSession? = localSession

    internal suspend fun transportSession(): ApiSession {
        return checkNotNull(localSession) { "Rust HTTP server is not initialized" }
    }

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

    @Volatile internal var httpGeneration: Long = 0L
        private set
    private var lastHttpGeneration: Long = 0L
    private var sessionToken: String? = null
    private var collectorsStarted = false

    private val startupConfig by lazy {
        RustHttpServerConfig(isDebugBuild(), startupSystemProperties())
    }

    fun start() = lock.withLock {
        if (httpGeneration != 0L) return@withLock
        val token = sessionToken ?: generateChaCha20Key().also { sessionToken = it }
        val config = startupConfig
        val ports = JsonHelper.jsonDecode<RustHttpServerResult>(RustNativeApi.start(
            dataDir, token, JsonHelper.jsonEncode(config)))
        check(ports.errorCode.isEmpty()) { ports.error }
        require(ports.cacheDir.startsWith('/') && ports.imageCacheDir.startsWith('/') && ports.videoCacheDir.startsWith('/')) { "Rust cache paths required" }
        check(ports.cacheDir == cacheDir) { "Rust cache root differs from startup configuration" }
        require(ports.logFile.startsWith('/')) { "Rust log file path required" }
        require(ports.sharedPreviewDir.startsWith('/')) { "Rust shared preview cache path required" }
        logPath = ports.logFile
        imageCachePath = ports.imageCacheDir
        videoCachePath = ports.videoCacheDir
        sharedPreviewPath = ports.sharedPreviewDir
        kotlinx.coroutines.runBlocking(com.ismartcoding.plain.platform.IODispatcher) { com.ismartcoding.plain.preferences.RustSystemState.refresh() }
        localSession = ApiSession(
            "http://127.0.0.1:${ports.httpPort}",
            TempData.clientId,
            token,
        )
        if (!collectorsStarted) {
            RustEventHandler.start(scope)
            RustHostApiHandler.start(scope)
            collectorsStarted = true
        }
        httpGeneration = ports.generation
        lastHttpGeneration = maxOf(lastHttpGeneration, ports.generation)

    }

    val isHttpRunning: Boolean get() = httpGeneration != 0L

    internal suspend fun startHttp(): Unit = withIO { start() }

    internal suspend fun stopHttp(): Unit = withIO {
        lock.withLock {
            RustNativeApi.stop()
            httpGeneration = 0L
        }
    }

    internal fun httpStarted(generation: Long, httpPort: Int) = lock.withLock {
        if (generation <= lastHttpGeneration || localSession == null) return@withLock
        check(httpPort in 1..65535) { "Invalid HTTP port" }
        localSession = ApiSession(
            "http://127.0.0.1:$httpPort", TempData.clientId, checkNotNull(sessionToken),
        )
        lastHttpGeneration = generation
        httpGeneration = generation
    }

    internal fun httpFailed(generation: Long): Boolean = lock.withLock {
        if (httpGeneration == 0L || httpGeneration != generation) return@withLock false
        httpGeneration = 0L
        true
    }
}

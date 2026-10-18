package com.ismartcoding.plain.rust.request

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.lib.JsonHelper

import com.ismartcoding.plain.app.events.*
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.app.*
import com.ismartcoding.plain.ui.app.*
import com.ismartcoding.plain.features.chat.*
import com.ismartcoding.plain.ui.chat.*
import com.ismartcoding.plain.features.feeds.*
import com.ismartcoding.plain.ui.feeds.*
import com.ismartcoding.plain.features.images.search.*
import com.ismartcoding.plain.features.sessions.*
import com.ismartcoding.plain.ui.sessions.*
import com.ismartcoding.plain.features.shares.*
import com.ismartcoding.plain.ui.shares.*
import com.ismartcoding.plain.platform.*
import com.ismartcoding.plain.platform.contacts.*
import com.ismartcoding.plain.platform.files.*
import com.ismartcoding.plain.ui.platform.files.*
import com.ismartcoding.plain.platform.media.*
import com.ismartcoding.plain.ui.platform.media.*
import com.ismartcoding.plain.platform.network.*
import com.ismartcoding.plain.platform.permissions.*
import com.ismartcoding.plain.app.*
import com.ismartcoding.plain.ui.app.*
import com.ismartcoding.plain.ui.navigation.*
import com.ismartcoding.plain.features.sessions.*
import com.ismartcoding.plain.ui.sessions.*
import com.ismartcoding.plain.features.settings.*
import com.ismartcoding.plain.ui.settings.*
import com.ismartcoding.plain.platform.files.*
import com.ismartcoding.plain.ui.platform.files.*
import com.ismartcoding.plain.platform.media.*
import com.ismartcoding.plain.ui.platform.media.*
import com.ismartcoding.plain.platform.notifications.*
import com.ismartcoding.plain.platform.system.*
import com.ismartcoding.plain.ui.platform.system.*
import com.ismartcoding.plain.ui.state.*
import com.ismartcoding.plain.platform.telephony.*
import com.ismartcoding.plain.rust.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import kotlin.concurrent.Volatile

object RustContentApi {
    private val lock = PlatformLock()
    private val client by lazy { createPlainHttpClient(PlainHttpClientSpec.Local()) }

    /** Endpoint description of this device's own Rust server, for the REST calls below. */
    @Volatile private var localSession: ContentApiSession? = null

    internal fun startedSession(): ContentApiSession? = localSession

    internal suspend fun transportSession(): ContentApiSession {
        return checkNotNull(localSession) { "Rust HTTP server is not initialized" }
    }

    val directory: String by lazy { dataDirPath() }
    @Volatile private var logPath: String? = null
    val logFilePath: String get() = checkNotNull(logPath) { "Rust log path is not initialized" }
    @Volatile private var imageCachePath: String? = null
    @Volatile private var videoCachePath: String? = null
    @Volatile private var sharedPreviewPath: String? = null
    val cacheDirectory: String by lazy { cacheDirPath() }
    val videoCacheDirectory: String get() = checkNotNull(videoCachePath) { "Rust video cache is not initialized" }
    val imageCacheDirectory: String get() = checkNotNull(imageCachePath) { "Rust image cache is not initialized" }
    val sharedPreviewDirectory: String get() = checkNotNull(sharedPreviewPath) { "Rust shared preview cache is not initialized" }

    @Volatile internal var httpGeneration: Long = 0L
        private set
    private var lastHttpGeneration: Long = 0L
    private var sessionToken: String? = null
    private var collectorsStarted = false

    private val startupConfig by lazy {
        RustHttpServerConfig(isDebugBuild(), webAssetsSource(), startupSystemProperties())
    }

    fun start() = lock.withLock {
        if (httpGeneration != 0L) return@withLock
        val token = sessionToken ?: generateChaCha20Key().also { sessionToken = it }
        val config = startupConfig
        val ports = JsonHelper.jsonDecode<RustHttpServerResult>(PlainRust.initialize(
            directory, token, JsonHelper.jsonEncode(config)))
        check(ports.errorCode.isEmpty()) { ports.error }
        require(ports.cacheDir.startsWith('/') && ports.imageCacheDir.startsWith('/') && ports.videoCacheDir.startsWith('/')) { "Rust cache paths required" }
        check(ports.cacheDir == cacheDirectory) { "Rust cache root differs from startup configuration" }
        require(ports.logFile.startsWith('/')) { "Rust log file path required" }
        require(ports.sharedPreviewDir.startsWith('/')) { "Rust shared preview cache path required" }
        logPath = ports.logFile
        imageCachePath = ports.imageCacheDir
        videoCachePath = ports.videoCacheDir
        sharedPreviewPath = ports.sharedPreviewDir
        kotlinx.coroutines.runBlocking(com.ismartcoding.plain.platform.IODispatcher) { com.ismartcoding.plain.preferences.RustSystemState.refresh() }
        localSession = ContentApiSession(
            "http://127.0.0.1:${ports.httpPort}",
            TempData.clientId,
            token,
        )
        if (!collectorsStarted) {
            PlainRust.start()
            collectorsStarted = true
        }
        httpGeneration = ports.generation
        lastHttpGeneration = maxOf(lastHttpGeneration, ports.generation)

    }

    internal fun stopHttp() = lock.withLock {
        PlainRust.stopHttp()
        httpGeneration = 0L
    }

    internal fun httpStarted(generation: Long, httpPort: Int) = lock.withLock {
        if (generation <= lastHttpGeneration || localSession == null) return@withLock
        check(httpPort in 1..65535) { "Invalid HTTP port" }
        localSession = ContentApiSession(
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

    // ── GraphQL: delegated, the transport lives in PlainRust ─────────────

    suspend fun query(selection: String, session: ContentApiSession? = null): JsonObject =
        PlainRust.mainQuery(selection, session)

    suspend fun mutate(selection: String, session: ContentApiSession? = null, longRunning: Boolean = false): JsonObject =
        PlainRust.mainMutate(selection, session, longRunning)

    suspend fun graphql(document: String, variables: Map<String, JsonElement> = emptyMap(), session: ContentApiSession? = null): JsonObject =
        PlainRust.mainGraphql(document, variables, session)

    suspend fun publish(event: HostEvent): Int = PlainRust.publish(event)
}

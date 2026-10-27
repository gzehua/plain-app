package com.ismartcoding.plain.rust

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.Prefs

import com.ismartcoding.plain.platform.system.*
import com.ismartcoding.plain.rust.native.RustNativeBindings
import com.ismartcoding.plain.rust.host.RustHostApiHandler
import com.ismartcoding.plain.rust.host.audio.AudioCatalogObserver
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
    @Volatile private var sharedPreviewPath: String? = null
    val sharedPreviewDir: String get() = checkNotNull(sharedPreviewPath) { "Rust shared preview cache is not initialized" }

    private var sessionToken: String? = null
    private var collectorsStarted = false

    fun start(audioCatalogObserver: AudioCatalogObserver? = null) = lock.withLock {
        val token = sessionToken ?: generateChaCha20Key().also { sessionToken = it }
        val config = rustStartupConfig
        val result = JsonHelper.jsonDecode<RustStartupResult>(RustNativeBindings.startCore(
            dataDir, token, JsonHelper.jsonEncode(config)))
        check(result.errorCode.isEmpty()) { result.error }
        require(result.logFile.startsWith('/')) { "Rust log file path required" }
        require(result.sharedPreviewDir.startsWith('/')) { "Rust shared preview cache path required" }
        logPath = result.logFile
        sharedPreviewPath = result.sharedPreviewDir
        if (!collectorsStarted) {
            Prefs.load()
            isCoreInitialized = true
            RustHostApiHandler.start(scope, audioCatalogObserver)
            com.ismartcoding.plain.rust.graphql.RustGraphqlSubscriptions.start(scope)
            com.ismartcoding.plain.rust.graphql.AppSubscriptions.start(scope)
            com.ismartcoding.plain.app.SubscriptionObservers.start(scope)
            collectorsStarted = true
        }
    }
}

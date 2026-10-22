package com.ismartcoding.plain.app

import com.ismartcoding.plain.rust.native.RustNativeApi

import com.ismartcoding.plain.ui.i18n.LocaleHelper

import com.ismartcoding.plain.app.HttpServerManager
import com.ismartcoding.plain.rust.RustRuntime
import com.ismartcoding.plain.rust.RustHttpStatus

import com.ismartcoding.plain.features.settings.HttpServerState
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.http_server_failed
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.lib.logcat.LogCat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

suspend fun getSSLSignature(): ByteArray = RustNativeApi.tlsCertificateSignature()

suspend fun generateSSLKeyStore() {
    RustNativeApi.regenerateTlsCertificate()
}

/**
 * Source format of a user-provided SSL certificate for [replaceSSLKeyStoreAsync].
 */
enum class SslCertImportMode {
    /** Single PKCS#12 bundle (.p12/.pfx) + its password. */
    PKCS12,

    /** Two separate PEM files: certificate (CRT/PEM) + private key (KEY/PEM). */
    PEM,
}

/**
 * Replace the HTTPS keystore with a user-provided certificate.
 *
 * @param mode import format; see [SslCertImportMode]
 * @param firstUri URI of the picked file. For [SslCertImportMode.PKCS12] this is
 *        the PKCS#12 bundle; for [SslCertImportMode.PEM] it is the certificate file.
 * @param secondUri URI of the picked private key file (PEM mode only; ignored otherwise).
 * @param password password of the PKCS#12 bundle (PKCS12 mode only; ignored otherwise).
 * @return the raw signature bytes of the newly installed certificate
 * @throws Exception when the file cannot be read or parsed, the password is wrong,
 *         or no usable private key is found
 */
suspend fun replaceSSLKeyStoreAsync(
    mode: SslCertImportMode,
    firstUri: String,
    secondUri: String = "",
    password: String = "",
): ByteArray = withIO {
    when (mode) {
        SslCertImportMode.PKCS12 -> RustNativeApi.importTlsCertificatePkcs12(
            com.ismartcoding.plain.platform.files.readPickedFileBytes(firstUri), password,
        )
        SslCertImportMode.PEM -> RustNativeApi.importTlsCertificatePem(
            com.ismartcoding.plain.platform.files.readPickedFileBytes(firstUri).decodeToString(throwOnInvalidSequence = true),
            com.ismartcoding.plain.platform.files.readPickedFileBytes(secondUri).decodeToString(throwOnInvalidSequence = true),
        )
    }
}

/**
 * Reset the web console password to a new random value and persist it.
 * @return the new password
 */
suspend fun resetPasswordAsync(): String = com.ismartcoding.plain.features.sessions.RustWebLogin.resetPassword()

expect suspend fun onHttpServerStarted()
expect suspend fun onHttpServerStopped()
expect fun startHttpServerService()
expect suspend fun stopHttpServiceAsync()

/** Rust probes the public listener over loopback HTTP, not just its in-memory state. */
suspend fun checkHttpServerAsync(): Boolean = withIO {
    if (!RustRuntime.isCoreInitialized) return@withIO false
    try {
        RustNativeApi.checkHttp()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        LogCat.e("Rust HTTP server diagnostic failed: ${error.message}")
        false
    }
}

private val lifecycleMutex = Mutex()
private const val STOP_HOOK_TIMEOUT_MS = 2_000L

suspend fun startHttpServerAsync() = withIO {
    lifecycleMutex.withLock {
        if (HttpServerManager.serverState.value == HttpServerState.ON) return@withLock
        HttpServerManager.serverState.value = HttpServerState.STARTING
        HttpServerManager.httpServerError.value = ""
        try {
            RustRuntime.start()
            applyHttpServerStatus()
        } catch (error: Throwable) {
            withContext(NonCancellable) {
                stopEngineAndHooks()
                if (error is CancellationException) {
                    HttpServerManager.serverState.value = HttpServerState.OFF
                } else {
                    recordServerFailure(error.message ?: error::class.simpleName.orEmpty())
                }
            }
            if (error is CancellationException) throw error
        }
    }
}

private suspend fun stopHooks() {
    runCatching { withTimeout(STOP_HOOK_TIMEOUT_MS) { onHttpServerStopped() } }
        .onFailure { LogCat.e("HTTP server stop hook failed: ${it.message}") }
}

private suspend fun stopEngineAndHooks() {
    runCatching { RustNativeApi.stop() }
        .onFailure { LogCat.e("HTTP server engine stop failed: ${it.message}") }
    stopHooks()
}

private suspend fun recordServerFailure(detail: String) {
    LogCat.e("HTTP server failed: $detail")
    val message = runCatching { LocaleHelper.getStringAsync(Res.string.http_server_failed) }
        .getOrDefault("HTTP server failed")
    HttpServerManager.httpServerError.value = if (detail.isEmpty()) message else "$message ($detail)"
    HttpServerManager.serverState.value = HttpServerState.ERROR
}

internal suspend fun refreshHttpServerState() = withIO {
    if (!RustRuntime.isCoreInitialized) return@withIO
    withContext(NonCancellable) {
        lifecycleMutex.withLock { applyHttpServerStatus() }
    }
}

private suspend fun applyHttpServerStatus() {
    // Read Rust's current state after lifecycle commands, never replay an old event payload.
    val status = JsonHelper.jsonDecode<RustHttpStatus>(RustNativeApi.httpStatus())
    when (status.state) {
        "started" -> {
            if (HttpServerManager.serverState.value != HttpServerState.ON) onHttpServerStarted()
            HttpServerManager.httpServerError.value = ""
            HttpServerManager.serverState.value = HttpServerState.ON
        }
        "error" -> {
            stopHooks()
            recordServerFailure(status.message)
        }
        "stopped" -> {
            // Startup cleanup must not erase the error already reported by the start command.
            if (HttpServerManager.serverState.value == HttpServerState.ERROR ||
                HttpServerManager.serverState.value == HttpServerState.OFF) return
            stopHooks()
            HttpServerManager.httpServerError.value = ""
            HttpServerManager.serverState.value = HttpServerState.OFF
        }
        else -> error("Unknown Rust HTTP state: ${status.state}")
    }
}

internal suspend fun finishHttpServerStopAsync() = withIO {
    withContext(NonCancellable) {
        lifecycleMutex.withLock {
            stopEngineAndHooks()
            HttpServerManager.httpServerError.value = ""
            HttpServerManager.serverState.value = HttpServerState.OFF
        }
    }
}

suspend fun stopHttpServerCoreAsync() = withIO {
    withContext(NonCancellable) {
        HttpServerManager.serverState.value = HttpServerState.STOPPING
        finishHttpServerStopAsync()
    }
}

fun restartServer() {
    coIO {
        lifecycleMutex.withLock {
            try {
                RustNativeApi.restartHttpServer()
                applyHttpServerStatus()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                recordServerFailure(error.message.orEmpty())
            }
        }
    }
}

fun isHttpServerRunning(): Boolean = HttpServerManager.serverState.value == HttpServerState.ON
fun isMdnsRunning(): Boolean = com.ismartcoding.plain.features.nearby.RustMdnsRuntime.running
expect fun getAwareAttachStatus(): String
expect fun getAwareDiscoveredPeerCount(): Int

package com.ismartcoding.plain.rust.request

import com.ismartcoding.plain.features.settings.AppUpdateDownload
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.bridge.RustCoreBridge
import com.ismartcoding.plain.rust.bridge.RustDispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

internal object NativeManagementClient {
    internal suspend inline fun <reified T : Any> call(crossinline request: () -> String): T = withContext(RustDispatchers.requests) {
        val reply = JsonHelper.jsonDecode<NativeManagementReply<T>>(request())
        reply.error?.let { error(it) }
        requireNotNull(reply.value) { "Native management response missing value" }
    }

    suspend fun restartHttpServer(httpPort: Int? = null, httpsPort: Int? = null): HttpServerRestart = call { RustCoreBridge.restartHttpServer(httpPort ?: -1, httpsPort ?: -1) }
    suspend fun regenerateTlsCertificate(): List<Int> = call { RustCoreBridge.regenerateTlsCertificate() }
    suspend fun importTlsCertificatePem(certificatePem: String, privateKeyPem: String): List<Int> = call { RustCoreBridge.importTlsCertificatePem(certificatePem, privateKeyPem) }
    suspend fun importTlsCertificatePkcs12(data: ByteArray, password: String): List<Int> = call { RustCoreBridge.importTlsCertificatePkcs12(data, password) }
    suspend fun createSession(name: String): com.ismartcoding.plain.features.sessions.CreatedSession = call { RustCoreBridge.createSession(name) }
    suspend fun confirmWebLogin(requestId: String, allowed: Boolean): Boolean = call { RustCoreBridge.confirmWebLogin(requestId, allowed) }
    suspend fun stageAppRestore(path: String): Boolean = call { RustCoreBridge.stageAppRestore(path) }
    suspend fun exportAppBackup(): String = call { RustCoreBridge.exportAppBackup() }
    suspend fun createCrashReport(report: String): String = call { RustCoreBridge.createCrashReport(report) }
    suspend fun systemSettings(): com.ismartcoding.plain.preferences.SystemSettings = call { RustCoreBridge.systemSettings() }
    suspend fun userSettings(): kotlinx.serialization.json.JsonObject = call { RustCoreBridge.userSettings() }
    suspend fun toggleNotificationApp(packageName: String): Boolean = call { RustCoreBridge.toggleNotificationApp(packageName) }
    suspend fun verifyAdbToken(token: String): Boolean = call { RustCoreBridge.verifyAdbToken(token) }
    suspend fun startAppUpdateDownload(): AppUpdateDownload = call { RustCoreBridge.startAppUpdateDownload() }
    suspend fun cancelAppUpdateDownload(): AppUpdateDownload = call { RustCoreBridge.cancelAppUpdateDownload() }
    suspend fun checkAppUpdate(automatic: Boolean): String = call { RustCoreBridge.checkAppUpdate(automatic) }
}

@Serializable
internal data class NativeManagementReply<T>(val value: T? = null, val error: String? = null)

@Serializable
internal data class HttpServerRestart(
    val revision: Long,
    val restarting: Boolean,
    val httpPort: Int,
    val httpsPort: Int,
    val error: String,
)

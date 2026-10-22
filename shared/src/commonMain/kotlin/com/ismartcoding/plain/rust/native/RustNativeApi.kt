package com.ismartcoding.plain.rust.native

import com.ismartcoding.plain.features.sessions.CreatedSession
import com.ismartcoding.plain.features.settings.AppUpdateDownload
import com.ismartcoding.plain.preferences.SystemSettings
import kotlinx.serialization.json.JsonObject

/** Local-only JNI (Android) / C (iOS) boundary; no HTTP or session routing. */
internal expect object RustNativeApi {
    fun startCore(dataDir: String, token: String, configJson: String): String
    fun graphql(request: String): String
    fun next(stream: String): String
    fun send(action: String, message: String, bytes: ByteArray): String
    fun uploadChunk(id: String, index: Long, bytes: ByteArray): String
    fun resourceOpen(id: String): String
    fun resourceWrite(id: String, bytes: ByteArray): String
    fun resourceClose(id: String, failed: Boolean): String
    fun fileIcon(extension: String): String

    suspend fun restartHttpServer(): Boolean
    fun httpStatus(): String
    suspend fun bleIncoming(remoteHost: String, characteristicUuid: String, bytes: ByteArray): ByteArray
    suspend fun checkHttp(): Boolean
    suspend fun tlsCertificateSignature(): ByteArray
    suspend fun regenerateTlsCertificate(): ByteArray
    suspend fun importTlsCertificatePem(certificatePem: String, privateKeyPem: String): ByteArray
    suspend fun importTlsCertificatePkcs12(data: ByteArray, password: String): ByteArray
    suspend fun createSession(name: String): CreatedSession
    suspend fun confirmWebLogin(requestId: String, allowed: Boolean): Boolean
    suspend fun stageAppRestore(path: String): Boolean
    suspend fun exportAppBackup(): String
    suspend fun createCrashReport(report: String): String
    suspend fun systemSettings(): SystemSettings
    suspend fun userSettings(): JsonObject
    suspend fun toggleNotificationApp(packageName: String): Boolean
    suspend fun verifyAdbToken(token: String): Boolean
    suspend fun startAppUpdateDownload(): AppUpdateDownload
    suspend fun cancelAppUpdateDownload(): AppUpdateDownload
    suspend fun checkAppUpdate(automatic: Boolean): String
}

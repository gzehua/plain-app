package com.ismartcoding.plain.rust.native

import com.ismartcoding.plain.features.sessions.CreatedSession
import com.ismartcoding.plain.features.settings.AppUpdateDownload
import kotlinx.serialization.json.JsonElement
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
    suspend fun lanIpv4Addresses(): List<String>
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
    suspend fun completeOnboarding(): Boolean
    fun loadPreferencesResponse(): String
    suspend fun setPref(key: String, value: JsonElement): Boolean
    suspend fun toggleNotificationApp(packageName: String): Boolean
    suspend fun verifyAdbToken(token: String): Boolean
    suspend fun startAppUpdateDownload(): AppUpdateDownload
    suspend fun cancelAppUpdateDownload(): AppUpdateDownload
    suspend fun checkAppUpdate(automatic: Boolean): String

    suspend fun resetPassword(): Boolean
    suspend fun resetUrlToken(): Boolean
    suspend fun resetAdbToken(): Boolean
    suspend fun setNotificationFilterMode(mode: String): Boolean
    suspend fun clearNotificationFilterApps(): Boolean
    suspend fun isPairingRequestCurrent(id: String, signature: String): Boolean
    suspend fun reconnectPeers(): Boolean
    suspend fun filePaths(paths: List<String>): List<String?>
    suspend fun importNoteImage(source: String, fileName: String): String
    suspend fun importPomodoroSound(source: String): String
    suspend fun dlnaReceiver(): JsonObject
    suspend fun retryDlnaReceiver(): JsonObject
    suspend fun removeDlnaRule(ip: String, allowed: Boolean): JsonObject
    suspend fun acceptDlnaCast(remember: Boolean): JsonObject
    suspend fun rejectDlnaCast(remember: Boolean): JsonObject
    suspend fun scanHistory(): List<String>
    suspend fun addScanHistory(text: String): Boolean
    suspend fun removeScanHistory(text: String): Boolean
    suspend fun acquireMdnsDebugSession(token: String): JsonObject
    suspend fun releaseMdnsDebugSession(token: String): JsonObject
}

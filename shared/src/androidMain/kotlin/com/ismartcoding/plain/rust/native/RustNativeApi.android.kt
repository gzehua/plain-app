package com.ismartcoding.plain.rust.native

import com.ismartcoding.plain.lib.JsonHelper

import com.ismartcoding.plain.features.sessions.CreatedSession
import com.ismartcoding.plain.features.settings.AppUpdateDownload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

internal actual object RustNativeApi {
    init { System.loadLibrary("plain_rust") }
    private external fun fileIconNative(extension: String): String
    actual fun fileIcon(extension: String): String = fileIconNative(extension)
    private external fun startCoreNative(dataDir: String, token: String, configJson: String): String
    actual fun startCore(dataDir: String, token: String, configJson: String): String = startCoreNative(dataDir, token, configJson)
    private external fun graphqlNative(request: String): String
    actual fun graphql(request: String): String = graphqlNative(request)
    private external fun nextNative(stream: String): String
    private external fun sendNative(action: String, message: String, bytes: ByteArray): String
    actual fun next(stream: String): String = nextNative(stream)
    actual fun send(action: String, message: String, bytes: ByteArray): String = sendNative(action, message, bytes)
    private external fun uploadChunkNative(id: String, index: Long, bytes: ByteArray): String
    actual fun uploadChunk(id: String, index: Long, bytes: ByteArray): String = uploadChunkNative(id, index, bytes)
    private external fun resourceOpenNative(id: String): String
    private external fun resourceWriteNative(id: String, bytes: ByteArray): String
    private external fun resourceFileNative(id: String, fd: Int, offset: Long, length: Long): String
    fun resourceFile(id: String, fd: Int, offset: Long, length: Long): String = resourceFileNative(id, fd, offset, length)
    private external fun resourceCloseNative(id: String, failed: Boolean): String
    actual fun resourceOpen(id: String): String = resourceOpenNative(id)
    actual fun resourceWrite(id: String, bytes: ByteArray): String = resourceWriteNative(id, bytes)
    actual fun resourceClose(id: String, failed: Boolean): String = resourceCloseNative(id, failed)

    private external fun restartHttpServerNative(): String
    actual suspend fun restartHttpServer(): Boolean = nativeCall { restartHttpServerNative() }
    private external fun httpStatusNative(): String
    actual fun httpStatus(): String = httpStatusNative()
    private external fun bleIncomingNative(remoteHost: String, characteristicUuid: String, bytes: ByteArray): ByteArray
    actual suspend fun bleIncoming(remoteHost: String, characteristicUuid: String, bytes: ByteArray): ByteArray =
        withContext(Dispatchers.IO) { bleIncomingNative(remoteHost, characteristicUuid, bytes) }
    private external fun checkHttpNative(): String
    actual suspend fun checkHttp(): Boolean = nativeCall { checkHttpNative() }
    private external fun tlsCertificateSignatureNative(): String
    actual suspend fun tlsCertificateSignature(): ByteArray = nativeCertificate { tlsCertificateSignatureNative() }
    private external fun regenerateTlsCertificateNative(): String
    actual suspend fun regenerateTlsCertificate(): ByteArray = nativeCertificate { regenerateTlsCertificateNative() }
    private external fun importTlsCertificatePemNative(certificatePem: String, privateKeyPem: String): String
    actual suspend fun importTlsCertificatePem(certificatePem: String, privateKeyPem: String): ByteArray = nativeCertificate { importTlsCertificatePemNative(certificatePem, privateKeyPem) }
    private external fun importTlsCertificatePkcs12Native(data: ByteArray, password: String): String
    actual suspend fun importTlsCertificatePkcs12(data: ByteArray, password: String): ByteArray = nativeCertificate { importTlsCertificatePkcs12Native(data, password) }
    private external fun createSessionNative(name: String): String
    actual suspend fun createSession(name: String): CreatedSession = nativeCall { createSessionNative(name) }
    private external fun confirmWebLoginNative(requestId: String, allowed: Boolean): String
    actual suspend fun confirmWebLogin(requestId: String, allowed: Boolean): Boolean = nativeCall { confirmWebLoginNative(requestId, allowed) }
    private external fun stageAppRestoreNative(path: String): String
    actual suspend fun stageAppRestore(path: String): Boolean = nativeCall { stageAppRestoreNative(path) }
    private external fun exportAppBackupNative(): String
    actual suspend fun exportAppBackup(): String = nativeCall { exportAppBackupNative() }
    private external fun createCrashReportNative(report: String): String
    actual suspend fun createCrashReport(report: String): String = nativeCall { createCrashReportNative(report) }
    private external fun completeOnboardingNative(): String
    actual suspend fun completeOnboarding(): Boolean = nativeCall { completeOnboardingNative() }
    private external fun setPrefNative(key: String, value: String): String
    actual suspend fun setPref(key: String, value: JsonElement): Boolean = nativeCall { setPrefNative(key, value.toString()) }
    private external fun getPrefNative(key: String): String
    actual fun getPrefResponse(key: String): String = getPrefNative(key)

    private external fun preferencesNative(): String
    actual suspend fun preferences(): JsonObject = nativeCall { preferencesNative() }
    private external fun toggleNotificationAppNative(packageName: String): String
    actual suspend fun toggleNotificationApp(packageName: String): Boolean = nativeCall { toggleNotificationAppNative(packageName) }
    private external fun verifyAdbTokenNative(token: String): String
    actual suspend fun verifyAdbToken(token: String): Boolean = nativeCall { verifyAdbTokenNative(token) }
    private external fun startAppUpdateDownloadNative(): String
    actual suspend fun startAppUpdateDownload(): AppUpdateDownload = nativeCall { startAppUpdateDownloadNative() }
    private external fun cancelAppUpdateDownloadNative(): String
    actual suspend fun cancelAppUpdateDownload(): AppUpdateDownload = nativeCall { cancelAppUpdateDownloadNative() }
    private external fun checkAppUpdateNative(automatic: Boolean): String
    actual suspend fun checkAppUpdate(automatic: Boolean): String = nativeCall { checkAppUpdateNative(automatic) }

    private external fun resetPasswordNative(): String
    actual suspend fun resetPassword(): Boolean = nativeCall { resetPasswordNative() }
    private external fun resetUrlTokenNative(): String
    actual suspend fun resetUrlToken(): Boolean = nativeCall { resetUrlTokenNative() }
    private external fun resetAdbTokenNative(): String
    actual suspend fun resetAdbToken(): Boolean = nativeCall { resetAdbTokenNative() }
    private external fun setNotificationFilterModeNative(mode: String): String
    actual suspend fun setNotificationFilterMode(mode: String): Boolean = nativeCall { setNotificationFilterModeNative(mode) }
    private external fun clearNotificationFilterAppsNative(): String
    actual suspend fun clearNotificationFilterApps(): Boolean = nativeCall { clearNotificationFilterAppsNative() }
    private external fun isPairingRequestCurrentNative(id: String, signature: String): String
    actual suspend fun isPairingRequestCurrent(id: String, signature: String): Boolean = nativeCall { isPairingRequestCurrentNative(id, signature) }
    private external fun reconnectPeersNative(): String
    actual suspend fun reconnectPeers(): Boolean = nativeCall { reconnectPeersNative() }
    private external fun filePathsNative(paths: String): String
    actual suspend fun filePaths(paths: List<String>): List<String?> = nativeCall { filePathsNative(JsonHelper.jsonEncode(paths)) }
    private external fun importNoteImageNative(source: String, fileName: String): String
    actual suspend fun importNoteImage(source: String, fileName: String): String = nativeCall { importNoteImageNative(source, fileName) }
    private external fun importPomodoroSoundNative(source: String): String
    actual suspend fun importPomodoroSound(source: String): String = nativeCall { importPomodoroSoundNative(source) }
    private external fun dlnaReceiverNative(): String
    actual suspend fun dlnaReceiver(): JsonObject = nativeCall { dlnaReceiverNative() }
    private external fun retryDlnaReceiverNative(): String
    actual suspend fun retryDlnaReceiver(): JsonObject = nativeCall { retryDlnaReceiverNative() }
    private external fun removeDlnaRuleNative(ip: String, allowed: Boolean): String
    actual suspend fun removeDlnaRule(ip: String, allowed: Boolean): JsonObject = nativeCall { removeDlnaRuleNative(ip, allowed) }
    private external fun acceptDlnaCastNative(remember: Boolean): String
    actual suspend fun acceptDlnaCast(remember: Boolean): JsonObject = nativeCall { acceptDlnaCastNative(remember) }
    private external fun rejectDlnaCastNative(remember: Boolean): String
    actual suspend fun rejectDlnaCast(remember: Boolean): JsonObject = nativeCall { rejectDlnaCastNative(remember) }
    private external fun scanHistoryNative(): String
    actual suspend fun scanHistory(): List<String> = nativeCall { scanHistoryNative() }
    private external fun addScanHistoryNative(text: String): String
    actual suspend fun addScanHistory(text: String): Boolean = nativeCall { addScanHistoryNative(text) }
    private external fun removeScanHistoryNative(text: String): String
    actual suspend fun removeScanHistory(text: String): Boolean = nativeCall { removeScanHistoryNative(text) }
    private external fun acquireMdnsDebugSessionNative(token: String): String
    actual suspend fun acquireMdnsDebugSession(token: String): JsonObject = nativeCall { acquireMdnsDebugSessionNative(token) }
    private external fun releaseMdnsDebugSessionNative(token: String): String
    actual suspend fun releaseMdnsDebugSession(token: String): JsonObject = nativeCall { releaseMdnsDebugSessionNative(token) }
}

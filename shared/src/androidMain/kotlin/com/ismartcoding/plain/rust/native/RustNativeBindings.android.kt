package com.ismartcoding.plain.rust.native

internal actual object RustNativeBindings {
    init { System.loadLibrary("plain_rust") }

    actual external fun fileIcon(extension: String): String
    actual external fun startCore(dataDir: String, token: String, configJson: String): String
    actual external fun graphql(request: String): String
    actual external fun next(stream: String): String
    actual external fun send(action: String, message: String, bytes: ByteArray): String
    actual external fun uploadChunk(id: String, index: Long, bytes: ByteArray): String
    actual external fun resourceOpen(id: String): String
    actual external fun resourceWrite(id: String, bytes: ByteArray): String
    actual external fun resourceFile(id: String, fd: Int, offset: Long, length: Long): String
    actual external fun resourceClose(id: String, failed: Boolean): String
    actual external fun restartHttpServer(): String
    actual external fun httpStatus(): String
    actual external fun lanIpv4Addresses(): String
    actual external fun bleIncoming(remoteHost: String, characteristicUuid: String, bytes: ByteArray): ByteArray
    actual external fun checkHttp(): String
    actual external fun tlsCertificateSignature(): String
    actual external fun regenerateTlsCertificate(): String
    actual external fun importTlsCertificatePem(certificatePem: String, privateKeyPem: String): String
    actual external fun importTlsCertificatePkcs12(data: ByteArray, password: String): String
    actual external fun createSession(name: String): String
    actual external fun confirmWebLogin(requestId: String, allowed: Boolean): String
    actual external fun stageAppRestore(path: String): String
    actual external fun exportAppBackup(): String
    actual external fun createCrashReport(report: String): String
    actual external fun completeOnboarding(): String
    actual external fun setPref(key: String, value: String): String
    actual external fun loadPreferencesResponse(): String
    actual external fun toggleNotificationApp(packageName: String): String
    actual external fun verifyAdbToken(token: String): String
    actual external fun startAppUpdateDownload(): String
    actual external fun cancelAppUpdateDownload(): String
    actual external fun checkAppUpdate(automatic: Boolean): String
    actual external fun resetPassword(): String
    actual external fun resetUrlToken(): String
    actual external fun resetAdbToken(): String
    actual external fun setNotificationFilterMode(mode: String): String
    actual external fun clearNotificationFilterApps(): String
    actual external fun isPairingRequestCurrent(id: String, signature: String): String
    actual external fun reconnectPeers(): String
    actual external fun filePaths(paths: String): String
    actual external fun importNoteImage(source: String, fileName: String): String
    actual external fun importPomodoroSound(source: String): String
    actual external fun dlnaReceiver(): String
    actual external fun retryDlnaReceiver(): String
    actual external fun removeDlnaRule(ip: String, allowed: Boolean): String
    actual external fun acceptDlnaCast(remember: Boolean): String
    actual external fun rejectDlnaCast(remember: Boolean): String
    actual external fun scanHistory(): String
    actual external fun addScanHistory(text: String): String
    actual external fun removeScanHistory(text: String): String
    actual external fun acquireMdnsDebugSession(token: String): String
    actual external fun releaseMdnsDebugSession(token: String): String
}

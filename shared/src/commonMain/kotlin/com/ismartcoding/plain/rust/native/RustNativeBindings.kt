package com.ismartcoding.plain.rust.native

internal expect object RustNativeBindings {
    fun fileIcon(extension: String): String
    fun startCore(dataDir: String, token: String, configJson: String): String
    fun graphql(request: String): String
    fun next(stream: String): String
    fun send(action: String, message: String, bytes: ByteArray): String
    fun uploadChunk(id: String, index: Long, bytes: ByteArray): String
    fun resourceOpen(id: String): String
    fun resourceWrite(id: String, bytes: ByteArray): String
    fun resourceFile(id: String, fd: Int, offset: Long, length: Long): String
    fun resourceClose(id: String, failed: Boolean): String
    fun restartHttpServer(): String
    fun httpStatus(): String
    fun lanIpv4Addresses(): String
    fun bleIncoming(remoteHost: String, characteristicUuid: String, bytes: ByteArray): ByteArray
    fun checkHttp(): String
    fun tlsCertificateSignature(): String
    fun regenerateTlsCertificate(): String
    fun importTlsCertificatePem(certificatePem: String, privateKeyPem: String): String
    fun importTlsCertificatePkcs12(data: ByteArray, password: String): String
    fun createSession(name: String): String
    fun confirmWebLogin(requestId: String, allowed: Boolean): String
    fun stageAppRestore(path: String): String
    fun exportAppBackup(): String
    fun createCrashReport(report: String): String
    fun completeOnboarding(): String
    fun setPref(key: String, value: String): String
    fun loadPreferencesResponse(): String
    fun toggleNotificationApp(packageName: String): String
    fun verifyAdbToken(token: String): String
    fun startAppUpdateDownload(): String
    fun cancelAppUpdateDownload(): String
    fun checkAppUpdate(automatic: Boolean): String
    fun resetPassword(): String
    fun resetUrlToken(): String
    fun resetAdbToken(): String
    fun setNotificationFilterMode(mode: String): String
    fun clearNotificationFilterApps(): String
    fun isPairingRequestCurrent(id: String, signature: String): String
    fun reconnectPeers(): String
    fun filePaths(paths: String): String
    fun importNoteImage(source: String, fileName: String): String
    fun importPomodoroSound(source: String): String
    fun dlnaReceiver(): String
    fun retryDlnaReceiver(): String
    fun removeDlnaRule(ip: String, allowed: Boolean): String
    fun acceptDlnaCast(remember: Boolean): String
    fun rejectDlnaCast(remember: Boolean): String
    fun scanHistory(): String
    fun addScanHistory(text: String): String
    fun removeScanHistory(text: String): String
    fun acquireMdnsDebugSession(token: String): String
    fun releaseMdnsDebugSession(token: String): String
}

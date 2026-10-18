package com.ismartcoding.plain.rust.bridge

internal actual object RustCoreBridge {
    init { System.loadLibrary("plain_rust") }
    private external fun fileIconNative(extension: String): String
    actual fun fileIcon(extension: String): String = fileIconNative(extension)
    private external fun startNative(dataDir: String, token: String, configJson: String): String
    private external fun stopNative(): String
    actual fun start(dataDir: String, token: String, configJson: String): String = startNative(dataDir, token, configJson)
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
    actual fun stop() { val error = stopNative(); check(error.isEmpty()) { error } }

    private external fun restartHttpServerNative(httpPort: Int, httpsPort: Int): String
    actual fun restartHttpServer(httpPort: Int, httpsPort: Int): String = restartHttpServerNative(httpPort, httpsPort)
    private external fun regenerateTlsCertificateNative(): String
    actual fun regenerateTlsCertificate(): String = regenerateTlsCertificateNative()
    private external fun importTlsCertificatePemNative(certificatePem: String, privateKeyPem: String): String
    actual fun importTlsCertificatePem(certificatePem: String, privateKeyPem: String): String = importTlsCertificatePemNative(certificatePem, privateKeyPem)
    private external fun importTlsCertificatePkcs12Native(data: ByteArray, password: String): String
    actual fun importTlsCertificatePkcs12(data: ByteArray, password: String): String = importTlsCertificatePkcs12Native(data, password)
    private external fun createSessionNative(name: String): String
    actual fun createSession(name: String): String = createSessionNative(name)
    private external fun confirmWebLoginNative(requestId: String, allowed: Boolean): String
    actual fun confirmWebLogin(requestId: String, allowed: Boolean): String = confirmWebLoginNative(requestId, allowed)
    private external fun stageAppRestoreNative(path: String): String
    actual fun stageAppRestore(path: String): String = stageAppRestoreNative(path)
    private external fun exportAppBackupNative(): String
    actual fun exportAppBackup(): String = exportAppBackupNative()
    private external fun createCrashReportNative(report: String): String
    actual fun createCrashReport(report: String): String = createCrashReportNative(report)
    private external fun systemSettingsNative(): String
    actual fun systemSettings(): String = systemSettingsNative()
    private external fun userSettingsNative(): String
    actual fun userSettings(): String = userSettingsNative()
    private external fun toggleNotificationAppNative(packageName: String): String
    actual fun toggleNotificationApp(packageName: String): String = toggleNotificationAppNative(packageName)
    private external fun verifyAdbTokenNative(token: String): String
    actual fun verifyAdbToken(token: String): String = verifyAdbTokenNative(token)
    private external fun startAppUpdateDownloadNative(): String
    actual fun startAppUpdateDownload(): String = startAppUpdateDownloadNative()
    private external fun cancelAppUpdateDownloadNative(): String
    actual fun cancelAppUpdateDownload(): String = cancelAppUpdateDownloadNative()
    private external fun checkAppUpdateNative(automatic: Boolean): String
    actual fun checkAppUpdate(automatic: Boolean): String = checkAppUpdateNative(automatic)
}

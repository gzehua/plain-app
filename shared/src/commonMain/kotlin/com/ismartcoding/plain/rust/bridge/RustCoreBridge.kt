package com.ismartcoding.plain.rust.bridge

internal expect object RustCoreBridge {
    fun start(dataDir: String, token: String, configJson: String): String
    fun graphql(request: String): String
    fun next(stream: String): String
    fun send(action: String, message: String, bytes: ByteArray): String
    fun uploadChunk(id: String, index: Long, bytes: ByteArray): String
    fun resourceOpen(id: String): String
    fun resourceWrite(id: String, bytes: ByteArray): String
    fun resourceClose(id: String, failed: Boolean): String
    fun fileIcon(extension: String): String
    fun stop()

    fun restartHttpServer(httpPort: Int, httpsPort: Int): String
    fun regenerateTlsCertificate(): String
    fun importTlsCertificatePem(certificatePem: String, privateKeyPem: String): String
    fun importTlsCertificatePkcs12(data: ByteArray, password: String): String
    fun createSession(name: String): String
    fun confirmWebLogin(requestId: String, allowed: Boolean): String
    fun stageAppRestore(path: String): String
    fun exportAppBackup(): String
    fun createCrashReport(report: String): String
    fun systemSettings(): String
    fun userSettings(): String
    fun toggleNotificationApp(packageName: String): String
    fun verifyAdbToken(token: String): String
    fun startAppUpdateDownload(): String
    fun cancelAppUpdateDownload(): String
    fun checkAppUpdate(automatic: Boolean): String
}

package com.ismartcoding.plain.rust.native

import com.ismartcoding.plain.features.sessions.CreatedSession
import com.ismartcoding.plain.features.settings.AppUpdateDownload
import com.ismartcoding.plain.preferences.SystemSettings
import kotlinx.serialization.json.JsonObject

import com.ismartcoding.plain.rustprefs.plain_system_settings
import com.ismartcoding.plain.rustprefs.plain_user_settings
import com.ismartcoding.plain.rustprefs.plain_toggle_notification_app
import com.ismartcoding.plain.rustprefs.plain_file_icon
import com.ismartcoding.plain.rustprefs.plain_restart_http_server
import com.ismartcoding.plain.rustprefs.plain_tls_certificate_signature
import com.ismartcoding.plain.rustprefs.plain_regenerate_tls_certificate
import com.ismartcoding.plain.rustprefs.plain_import_tls_certificate_pem
import com.ismartcoding.plain.rustprefs.plain_import_tls_certificate_pkcs12
import com.ismartcoding.plain.rustprefs.plain_create_session
import com.ismartcoding.plain.rustprefs.plain_confirm_web_login
import com.ismartcoding.plain.rustprefs.plain_stage_app_restore
import com.ismartcoding.plain.rustprefs.plain_export_app_backup
import com.ismartcoding.plain.rustprefs.plain_create_crash_report
import com.ismartcoding.plain.rustprefs.plain_verify_adb_token
import com.ismartcoding.plain.rustprefs.plain_start_app_update_download
import com.ismartcoding.plain.rustprefs.plain_cancel_app_update_download
import com.ismartcoding.plain.rustprefs.plain_check_app_update

import com.ismartcoding.plain.rustprefs.plain_events_next
import com.ismartcoding.plain.rustprefs.plain_events_send
import kotlinx.serialization.json.*
import com.ismartcoding.plain.rustprefs.plain_graphql
import com.ismartcoding.plain.rustprefs.plain_http_stop
import com.ismartcoding.plain.rustprefs.plain_core_start
import com.ismartcoding.plain.rustprefs.plain_prefs_string_free
import com.ismartcoding.plain.rustprefs.plain_resource_close
import com.ismartcoding.plain.rustprefs.plain_resource_file
import com.ismartcoding.plain.rustprefs.plain_resource_open
import com.ismartcoding.plain.rustprefs.plain_resource_write
import com.ismartcoding.plain.rustprefs.plain_upload_chunk
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
internal actual object RustNativeApi {
    actual fun fileIcon(extension: String): String = memScoped {
        val pointer = checkNotNull(plain_file_icon(extension.cstr.ptr))
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual fun start(dataDir: String, token: String, configJson: String): String = memScoped {
        val result = plain_core_start(dataDir.cstr.ptr, token.cstr.ptr, configJson.cstr.ptr) ?: error("Rust core returned no response")
        try { result.toKString() } finally { plain_prefs_string_free(result) }
    }
    actual fun graphql(request: String): String = memScoped {
        val result = checkNotNull(plain_graphql(request.cstr.ptr))
        try { result.toKString() } finally { plain_prefs_string_free(result) }
    }
    actual fun next(stream: String): String = memScoped {
        val pointer = checkNotNull(plain_events_next(stream.cstr.ptr))
        try { RustNativeReply.value(pointer.toKString()) } finally { plain_prefs_string_free(pointer) }
    }
    actual fun send(action: String, message: String, bytes: ByteArray): String = memScoped {
        bytes.usePinned { pinned ->
            val pointer = checkNotNull(plain_events_send(action.cstr.ptr, message.cstr.ptr,
                if (bytes.isEmpty()) null else pinned.addressOf(0).reinterpret(), bytes.size.toULong()))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual fun uploadChunk(id: String, index: Long, bytes: ByteArray): String = memScoped {
        bytes.usePinned { pinned ->
            val pointer = checkNotNull(plain_upload_chunk(id.cstr.ptr, index,
                if (bytes.isEmpty()) null else pinned.addressOf(0).reinterpret(), bytes.size.toULong()))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual fun resourceOpen(id: String): String = memScoped {
        val pointer = checkNotNull(plain_resource_open(id.cstr.ptr))
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    fun resourceFile(id: String, fd: Int, offset: Long, length: Long): String = memScoped {
        val result = checkNotNull(plain_resource_file(id.cstr.ptr, fd, offset, length))
        try { result.toKString() } finally { plain_prefs_string_free(result) }
    }
    actual fun resourceWrite(id: String, bytes: ByteArray): String = memScoped {
        bytes.usePinned { pinned ->
            val pointer = checkNotNull(plain_resource_write(id.cstr.ptr,
                if (bytes.isEmpty()) null else pinned.addressOf(0).reinterpret(), bytes.size.toULong()))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual fun resourceClose(id: String, failed: Boolean): String = memScoped {
        val pointer = checkNotNull(plain_resource_close(id.cstr.ptr, failed))
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual fun stop() {
        val result = plain_http_stop() ?: return
        try { error(result.toKString()) } finally { plain_prefs_string_free(result) }
    }

    actual fun restartHttpServer(httpPort: Int, httpsPort: Int): String = memScoped {
        val pointer = checkNotNull(plain_restart_http_server(httpPort, httpsPort))
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual suspend fun tlsCertificateSignature(): ByteArray = nativeCertificate {
        memScoped {
            val pointer = checkNotNull(plain_tls_certificate_signature())
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun regenerateTlsCertificate(): ByteArray = nativeCertificate {
        memScoped {
            val pointer = checkNotNull(plain_regenerate_tls_certificate())
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun importTlsCertificatePem(certificatePem: String, privateKeyPem: String): ByteArray = nativeCertificate {
        memScoped {
            val pointer = checkNotNull(plain_import_tls_certificate_pem(certificatePem.cstr.ptr, privateKeyPem.cstr.ptr))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun importTlsCertificatePkcs12(data: ByteArray, password: String): ByteArray = nativeCertificate {
        memScoped {
            data.usePinned { pinned ->
                val pointer = checkNotNull(plain_import_tls_certificate_pkcs12(if (data.isEmpty()) null else pinned.addressOf(0).reinterpret(), data.size.toULong(), password.cstr.ptr))
                try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
            }
        }
    }
    actual suspend fun createSession(name: String): CreatedSession = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_create_session(name.cstr.ptr))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun confirmWebLogin(requestId: String, allowed: Boolean): Boolean = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_confirm_web_login(requestId.cstr.ptr, allowed))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun stageAppRestore(path: String): Boolean = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_stage_app_restore(path.cstr.ptr))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun exportAppBackup(): String = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_export_app_backup())
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun createCrashReport(report: String): String = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_create_crash_report(report.cstr.ptr))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun systemSettings(): SystemSettings = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_system_settings())
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun userSettings(): JsonObject = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_user_settings())
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun toggleNotificationApp(packageName: String): Boolean = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_toggle_notification_app(packageName.cstr.ptr))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun verifyAdbToken(token: String): Boolean = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_verify_adb_token(token.cstr.ptr))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun startAppUpdateDownload(): AppUpdateDownload = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_start_app_update_download())
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun cancelAppUpdateDownload(): AppUpdateDownload = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_cancel_app_update_download())
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun checkAppUpdate(automatic: Boolean): String = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_check_app_update(automatic))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
}

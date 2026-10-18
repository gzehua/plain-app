package com.ismartcoding.plain.rust.bridge

import com.ismartcoding.plain.rustprefs.plain_system_settings
import com.ismartcoding.plain.rustprefs.plain_user_settings
import com.ismartcoding.plain.rustprefs.plain_toggle_notification_app
import com.ismartcoding.plain.rustprefs.plain_file_icon
import com.ismartcoding.plain.rustprefs.plain_restart_http_server
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
internal actual object RustCoreBridge {
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
        try { RustBridgeReply.value(pointer.toKString()) } finally { plain_prefs_string_free(pointer) }
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
    actual fun regenerateTlsCertificate(): String = memScoped {
        val pointer = checkNotNull(plain_regenerate_tls_certificate())
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual fun importTlsCertificatePem(certificatePem: String, privateKeyPem: String): String = memScoped {
        val pointer = checkNotNull(plain_import_tls_certificate_pem(certificatePem.cstr.ptr, privateKeyPem.cstr.ptr))
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual fun importTlsCertificatePkcs12(data: ByteArray, password: String): String = memScoped {
        data.usePinned { pinned ->
            val pointer = checkNotNull(plain_import_tls_certificate_pkcs12(if (data.isEmpty()) null else pinned.addressOf(0).reinterpret(), data.size.toULong(), password.cstr.ptr))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual fun createSession(name: String): String = memScoped {
        val pointer = checkNotNull(plain_create_session(name.cstr.ptr))
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual fun confirmWebLogin(requestId: String, allowed: Boolean): String = memScoped {
        val pointer = checkNotNull(plain_confirm_web_login(requestId.cstr.ptr, allowed))
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual fun stageAppRestore(path: String): String = memScoped {
        val pointer = checkNotNull(plain_stage_app_restore(path.cstr.ptr))
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual fun exportAppBackup(): String = memScoped {
        val pointer = checkNotNull(plain_export_app_backup())
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual fun createCrashReport(report: String): String = memScoped {
        val pointer = checkNotNull(plain_create_crash_report(report.cstr.ptr))
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual fun systemSettings(): String = memScoped {
        val pointer = checkNotNull(plain_system_settings())
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual fun userSettings(): String = memScoped {
        val pointer = checkNotNull(plain_user_settings())
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual fun toggleNotificationApp(packageName: String): String = memScoped {
        val pointer = checkNotNull(plain_toggle_notification_app(packageName.cstr.ptr))
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual fun verifyAdbToken(token: String): String = memScoped {
        val pointer = checkNotNull(plain_verify_adb_token(token.cstr.ptr))
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual fun startAppUpdateDownload(): String = memScoped {
        val pointer = checkNotNull(plain_start_app_update_download())
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual fun cancelAppUpdateDownload(): String = memScoped {
        val pointer = checkNotNull(plain_cancel_app_update_download())
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual fun checkAppUpdate(automatic: Boolean): String = memScoped {
        val pointer = checkNotNull(plain_check_app_update(automatic))
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
}

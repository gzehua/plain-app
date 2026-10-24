package com.ismartcoding.plain.rust.native

import com.ismartcoding.plain.rustprefs.plain_reset_password
import com.ismartcoding.plain.rustprefs.plain_reset_url_token
import com.ismartcoding.plain.rustprefs.plain_reset_adb_token
import com.ismartcoding.plain.rustprefs.plain_set_notification_filter_mode
import com.ismartcoding.plain.rustprefs.plain_clear_notification_filter_apps
import com.ismartcoding.plain.rustprefs.plain_is_pairing_request_current
import com.ismartcoding.plain.rustprefs.plain_reconnect_peers
import com.ismartcoding.plain.rustprefs.plain_file_paths
import com.ismartcoding.plain.rustprefs.plain_import_note_image
import com.ismartcoding.plain.rustprefs.plain_import_pomodoro_sound
import com.ismartcoding.plain.rustprefs.plain_dlna_receiver
import com.ismartcoding.plain.rustprefs.plain_retry_dlna_receiver
import com.ismartcoding.plain.rustprefs.plain_remove_dlna_rule
import com.ismartcoding.plain.rustprefs.plain_accept_dlna_cast
import com.ismartcoding.plain.rustprefs.plain_reject_dlna_cast
import com.ismartcoding.plain.rustprefs.plain_scan_history
import com.ismartcoding.plain.rustprefs.plain_add_scan_history
import com.ismartcoding.plain.rustprefs.plain_remove_scan_history
import com.ismartcoding.plain.rustprefs.plain_acquire_mdns_debug_session
import com.ismartcoding.plain.rustprefs.plain_release_mdns_debug_session

import com.ismartcoding.plain.lib.JsonHelper

import com.ismartcoding.plain.features.sessions.CreatedSession
import com.ismartcoding.plain.features.settings.AppUpdateDownload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject

import com.ismartcoding.plain.rustprefs.plain_complete_onboarding
import com.ismartcoding.plain.rustprefs.plain_set_pref
import com.ismartcoding.plain.rustprefs.plain_get_pref
import com.ismartcoding.plain.rustprefs.plain_preferences
import com.ismartcoding.plain.rustprefs.plain_toggle_notification_app
import com.ismartcoding.plain.rustprefs.plain_file_icon
import com.ismartcoding.plain.rustprefs.plain_restart_http_server
import com.ismartcoding.plain.rustprefs.plain_http_status
import com.ismartcoding.plain.rustprefs.plain_ble_incoming
import com.ismartcoding.plain.rustprefs.plain_ble_incoming_buffer_free
import com.ismartcoding.plain.rustprefs.plain_check_http
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
    actual fun startCore(dataDir: String, token: String, configJson: String): String = memScoped {
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

    actual suspend fun restartHttpServer(): Boolean = nativeCall {
        val pointer = checkNotNull(plain_restart_http_server())
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual fun httpStatus(): String = memScoped {
        val failure = alloc<CPointerVar<ByteVar>>()
        failure.value = null
        val pointer = plain_http_status(failure.ptr)
        try {
            failure.value?.let { error(it.toKString()) }
            checkNotNull(pointer) { "Rust core returned no HTTP status" }.toKString()
        } finally {
            plain_prefs_string_free(pointer)
            plain_prefs_string_free(failure.value)
        }
    }
    actual suspend fun bleIncoming(remoteHost: String, characteristicUuid: String, bytes: ByteArray): ByteArray =
        withContext(Dispatchers.IO) {
            memScoped {
                bytes.usePinned { pinned ->
                    val buffer = plain_ble_incoming(remoteHost.cstr.ptr, characteristicUuid.cstr.ptr,
                        if (bytes.isEmpty()) null else pinned.addressOf(0).reinterpret(), bytes.size.toULong())
                    try {
                        buffer.useContents {
                            val value = data?.reinterpret<ByteVar>()?.readBytes(len.toInt()) ?: ByteArray(0)
                            check(status > 0) { value.decodeToString() }
                            value
                        }
                    } finally { plain_ble_incoming_buffer_free(buffer) }
                }
            }
        }
    actual suspend fun checkHttp(): Boolean = nativeCall {
        val pointer = checkNotNull(plain_check_http())
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
    actual suspend fun completeOnboarding(): Boolean = nativeCall {
        val pointer = checkNotNull(plain_complete_onboarding())
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }
    actual suspend fun setPref(key: String, value: JsonElement): Boolean = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_set_pref(key.cstr.ptr, value.toString().cstr.ptr))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual fun getPrefResponse(key: String): String = memScoped {
        val pointer = checkNotNull(plain_get_pref(key.cstr.ptr))
        try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
    }

    actual suspend fun preferences(): JsonObject = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_preferences())
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

    actual suspend fun resetPassword(): Boolean = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_reset_password())
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun resetUrlToken(): Boolean = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_reset_url_token())
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun resetAdbToken(): Boolean = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_reset_adb_token())
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun setNotificationFilterMode(mode: String): Boolean = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_set_notification_filter_mode(mode.cstr.ptr))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun clearNotificationFilterApps(): Boolean = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_clear_notification_filter_apps())
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun isPairingRequestCurrent(id: String, signature: String): Boolean = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_is_pairing_request_current(id.cstr.ptr, signature.cstr.ptr))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun reconnectPeers(): Boolean = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_reconnect_peers())
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun filePaths(paths: List<String>): List<String?> = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_file_paths(JsonHelper.jsonEncode(paths).cstr.ptr))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun importNoteImage(source: String, fileName: String): String = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_import_note_image(source.cstr.ptr, fileName.cstr.ptr))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun importPomodoroSound(source: String): String = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_import_pomodoro_sound(source.cstr.ptr))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun dlnaReceiver(): JsonObject = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_dlna_receiver())
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun retryDlnaReceiver(): JsonObject = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_retry_dlna_receiver())
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun removeDlnaRule(ip: String, allowed: Boolean): JsonObject = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_remove_dlna_rule(ip.cstr.ptr, allowed))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun acceptDlnaCast(remember: Boolean): JsonObject = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_accept_dlna_cast(remember))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun rejectDlnaCast(remember: Boolean): JsonObject = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_reject_dlna_cast(remember))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun scanHistory(): List<String> = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_scan_history())
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun addScanHistory(text: String): Boolean = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_add_scan_history(text.cstr.ptr))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun removeScanHistory(text: String): Boolean = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_remove_scan_history(text.cstr.ptr))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun acquireMdnsDebugSession(token: String): JsonObject = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_acquire_mdns_debug_session(token.cstr.ptr))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
    actual suspend fun releaseMdnsDebugSession(token: String): JsonObject = nativeCall {
        memScoped {
            val pointer = checkNotNull(plain_release_mdns_debug_session(token.cstr.ptr))
            try { pointer.toKString() } finally { plain_prefs_string_free(pointer) }
        }
    }
}

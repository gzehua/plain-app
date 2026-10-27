package com.ismartcoding.plain

import com.ismartcoding.plain.rust.native.RustHostReports
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.i18n.*

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.ui.i18n.LocaleHelper
import com.ismartcoding.plain.ui.components.DialogHelper
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.permissions.isGranted

internal fun MainActivity.startScreenMirror(audio: Boolean) {
    if (audio && !Permission.RECORD_AUDIO.isGranted()) {
        recordAudioForMirror.launch(Manifest.permission.RECORD_AUDIO)
    } else {
        screenCapture.launch(mediaProjectionManager.createScreenCaptureIntent())
    }
}

internal fun MainActivity.requestScreenMirrorAudio() {
    if (Permission.RECORD_AUDIO.isGranted()) sendScreenMirrorAudioStatus(true)
    else recordAudioForMirrorLate.launch(Manifest.permission.RECORD_AUDIO)
}

internal fun MainActivity.sendScreenMirrorAudioStatus(granted: Boolean) {
    coIO { RustHostReports.screenMirrorAudioGranted(JsonHelper.jsonEncode(granted)) }
}

internal fun MainActivity.openAppDetailsSettingsForAudio() {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        appDetailsSettingsForAudioLauncher.launch(intent)
    } catch (e: Exception) {
        try {
            appDetailsSettingsForAudioLauncher.launch(Intent(Settings.ACTION_SETTINGS))
        } catch (e2: Exception) {
            DialogHelper.showMessage(LocaleHelper.getString(Res.string.open_permission_settings))
            sendScreenMirrorAudioStatus(false)
        }
    }
}

internal fun MainActivity.showRecordAudioPermissionSettingsGuide() {
    DialogHelper.showConfirmDialog(
        LocaleHelper.getString(Res.string.permission_required),
        LocaleHelper.getString(Res.string.screen_mirror_audio_permission_settings_message),
        confirmButton = Pair(LocaleHelper.getString(Res.string.view_in_settings)) { openAppDetailsSettingsForAudio() },
        dismissButton = Pair(LocaleHelper.getString(Res.string.cancel)) { sendScreenMirrorAudioStatus(false) },
    )
}

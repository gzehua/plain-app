package com.ismartcoding.plain.platform.permissions


import com.ismartcoding.plain.preferences.*

import com.ismartcoding.plain.app.events.RequestPermissionsEvent
import com.ismartcoding.plain.lib.sendEvent

/**
 * Check whether a runtime permission is granted without prompting the user.
 *
 * @param perm Android: `android.Manifest.permission.*` (e.g. "android.permission.CAMERA").
 *             iOS: empty string (returns true on iOS, all permissions queried via OS APIs).
 */
expect fun isPermissionGranted(perm: String): Boolean

enum class Permission {
    WRITE_EXTERNAL_STORAGE,
    READ_SMS,
    SEND_SMS,
    READ_CONTACTS,
    WRITE_CONTACTS,
    READ_CALL_LOG,
    WRITE_CALL_LOG,
    CALL_PHONE,
    POST_NOTIFICATIONS,
    NEARBY_WIFI_DEVICES,
    ACCESS_FINE_LOCATION,
    CAMERA,
    SYSTEM_ALERT_WINDOW,
    RECORD_AUDIO,
    READ_MEDIA_IMAGES,
    READ_MEDIA_VIDEOS,
    READ_MEDIA_AUDIO,
    NOTIFICATION_LISTENER,
    READ_PHONE_STATE,
    READ_PHONE_NUMBERS,
    SCHEDULE_EXACT_ALARM,
    QUERY_ALL_PACKAGES,
    ADB,
    CLIPBOARD
    ;


    fun toSysPermission(): String {
        return "android.permission.${this.name}"
    }

}

expect fun Permission.isGranted(): Boolean

/**
 * True when the user denied this permission before but the runtime dialog can
 * still be shown (i.e., not a permanent "don't ask again" denial). Only
 * meaningful after an actual denial; false on platforms without the concept.
 */
expect fun Permission.shouldShowRationale(): Boolean

/**
 * Ensure the POST_NOTIFICATIONS runtime permission is granted, prompting the
 * user if necessary. Returns true once the permission is granted (or already
 * was), false if the user denied it. No-op (always returns true) on platforms
 * without a notification permission concept (iOS).
 *
 * Equivalent to the Android-only `Permissions.ensureNotificationAsync(context)`
 * but uses the platform's app context internally so it can be called from
 * commonMain.
 */
expect suspend fun ensureNotificationPermissionAsync(): Boolean

/**
 * Check the POST_NOTIFICATIONS permission and run [onGranted] once it is granted
 * (immediately if already granted, after the user grants it via the dialog
 * otherwise). On iOS, [onGranted] is invoked immediately since iOS has no
 * equivalent runtime permission concept for notifications.
 *
 * Equivalent to the Android-only `Permissions.checkNotification(context, ...)`.
 *
 * @param stringResource The resource id of the message shown in the
 *     confirmation dialog when the permission is not yet granted.
 */


fun Permission.grant(): Boolean {
    if (isGranted()) return true
    sendEvent(RequestPermissionsEvent(this))
    return false
}

/**
 * Whether the user handed this permission to the web client. The list already
 * arrives with [com.ismartcoding.plain.api.graphql.models.SystemSettings] over the prefs
 * bridge, so this is a local read — the core has nothing to add.
 */
suspend fun Permission.isEnabledAsync(): Boolean =
    name in com.ismartcoding.plain.preferences.RustSystemState.state.value.apiPermissions

/** Enabled for the web client *and* granted by the OS. Both answers are local. */
suspend fun Permission.enabledAndIsGrantedAsync(): Boolean = isEnabledAsync() && isGranted()

suspend fun Permission.checkEnabledAsync() {
    if (!isEnabledAsync()) {
        throw Exception("no_permission")
    }
}

/**
 * Show a "permission denied" dialog with a "Go to Settings" button.
 * Called from iosMain (which cannot access `Res.string` directly) to
 * delegate the dialog UI to commonMain where Compose Resources are
 * accessible.
 *
 * @param onOpenSettings Called when the user taps "Go to Settings".
 */


/**
 * Show a notification permission confirmation dialog. Called from
 * iosMain's [checkNotificationPermission] to delegate the dialog UI
 * to commonMain where Compose Resources are accessible.
 *
 * @param messageResource The resource id of the dialog message.
 * @param onConfirmed Called when the user taps "OK" to proceed with
 *     requesting the notification permission.
 */


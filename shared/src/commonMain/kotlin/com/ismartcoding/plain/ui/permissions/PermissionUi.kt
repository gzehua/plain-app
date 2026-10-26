package com.ismartcoding.plain.ui.permissions

import com.ismartcoding.plain.ui.permissions.getText
import com.ismartcoding.plain.ui.permissions.getGrantAccessText

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import com.ismartcoding.plain.i18n.*
import com.ismartcoding.plain.platform.permissions.*
import com.ismartcoding.plain.platform.permissions.Permission.*
import com.ismartcoding.plain.ui.i18n.LocaleHelper
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.ui.components.DialogHelper

@Composable
    fun Permission.getText(): String {
        return when (this) {
            WRITE_EXTERNAL_STORAGE -> stringResource(Res.string.feature_WRITE_EXTERNAL_STORAGE)
            READ_SMS -> stringResource(Res.string.feature_READ_SMS)
            SEND_SMS -> stringResource(Res.string.feature_SEND_SMS)
            WRITE_CALL_LOG -> stringResource(Res.string.feature_WRITE_CALL_LOG)
            CALL_PHONE -> stringResource(Res.string.feature_CALL_PHONE)
            WRITE_CONTACTS -> stringResource(Res.string.feature_WRITE_CONTACTS)
            NOTIFICATION_LISTENER -> stringResource(Res.string.feature_NOTIFICATION_LISTENER)
            READ_PHONE_NUMBERS -> stringResource(Res.string.feature_READ_PHONE_NUMBERS)
            QUERY_ALL_PACKAGES -> stringResource(Res.string.feature_QUERY_ALL_PACKAGES)
            else -> ""
        }
    }

@Composable
    fun Permission.getGrantAccessText(): String {
        return when {
            this == READ_SMS -> stringResource(Res.string.need_sms_permission)
            this == READ_CALL_LOG -> stringResource(Res.string.need_call_permission)
            this == READ_CONTACTS -> stringResource(Res.string.need_contact_permission)
            this == WRITE_EXTERNAL_STORAGE -> stringResource(Res.string.need_storage_permission)
            else -> ""
        }
    }

fun checkNotificationPermission(stringResource: StringResource, onGranted: () -> Unit) {
    if (Permission.POST_NOTIFICATIONS.isGranted()) {
        onGranted()
    } else {
        showNotificationConfirmDialog(stringResource) {
            coIO {
                if (ensureNotificationPermissionAsync()) onGranted()
            }
        }
    }
}

fun showPermissionDeniedDialog(onOpenSettings: () -> Unit) {
    coIO {
        val message = LocaleHelper.getStringAsync(Res.string.permission_denied_message)
        val title = LocaleHelper.getStringAsync(Res.string.settings)
        val goText = LocaleHelper.getStringAsync(Res.string.go_to_settings)
        val okText = LocaleHelper.getStringAsync(Res.string.ok)
        DialogHelper.showConfirmDialog(
            title = title,
            message = message,
            confirmButton = Pair(goText) { onOpenSettings() },
            dismissButton = Pair(okText) {},
        )
    }
}

fun showNotificationConfirmDialog(
    messageResource: StringResource,
    onConfirmed: () -> Unit,
) {
    coIO {
        val message = LocaleHelper.getStringAsync(messageResource)
        val confirmText = LocaleHelper.getStringAsync(Res.string.confirm)
        val okText = LocaleHelper.getStringAsync(Res.string.ok)
        DialogHelper.showConfirmDialog(confirmText, message, confirmButton = Pair(okText) {
            onConfirmed()
        })
    }
}

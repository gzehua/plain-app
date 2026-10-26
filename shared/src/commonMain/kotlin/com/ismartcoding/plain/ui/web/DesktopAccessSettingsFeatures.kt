package com.ismartcoding.plain.ui.web

import androidx.compose.runtime.Composable
import com.ismartcoding.plain.features.web.WebSettingsFeature
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.clipboard_sync
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.ui.permissions.PermissionItem
import com.ismartcoding.plain.ui.permissions.getText
import org.jetbrains.compose.resources.stringResource

internal fun Permission.accessFeatureOf(): WebSettingsFeature? = when (this) {
    Permission.WRITE_EXTERNAL_STORAGE -> WebSettingsFeature.FILES
    Permission.WRITE_CONTACTS -> WebSettingsFeature.CONTACTS
    Permission.READ_SMS -> WebSettingsFeature.SMS
    Permission.WRITE_CALL_LOG -> WebSettingsFeature.CALL_LOGS
    Permission.CALL_PHONE -> WebSettingsFeature.CALL_PHONE
    Permission.READ_PHONE_NUMBERS -> WebSettingsFeature.PHONE_NUMBER
    Permission.QUERY_ALL_PACKAGES -> WebSettingsFeature.APPS
    Permission.NOTIFICATION_LISTENER -> WebSettingsFeature.NOTIFICATIONS
    else -> null
}

internal fun itemIndexFor(feature: WebSettingsFeature, rows: List<PermissionItem>, notificationsShown: Boolean): Int? {
    rows.forEachIndexed { i, m -> if (m.permission.accessFeatureOf() == feature) return 2 + i }
    var index = 2 + rows.size
    if (feature == WebSettingsFeature.NOTIFICATIONS) return if (notificationsShown) index else null
    if (notificationsShown) index++
    if (feature == WebSettingsFeature.CLIPBOARD) return index
    return null
}

@Composable
internal fun bubbleLabel(feature: WebSettingsFeature): String = when (feature) {
    WebSettingsFeature.FILES -> Permission.WRITE_EXTERNAL_STORAGE.getText()
    WebSettingsFeature.CONTACTS -> Permission.WRITE_CONTACTS.getText()
    WebSettingsFeature.SMS -> Permission.READ_SMS.getText()
    WebSettingsFeature.CALL_LOGS -> Permission.WRITE_CALL_LOG.getText()
    WebSettingsFeature.CALL_PHONE -> Permission.CALL_PHONE.getText()
    WebSettingsFeature.PHONE_NUMBER -> Permission.READ_PHONE_NUMBERS.getText()
    WebSettingsFeature.APPS -> Permission.QUERY_ALL_PACKAGES.getText()
    WebSettingsFeature.NOTIFICATIONS -> Permission.NOTIFICATION_LISTENER.getText()
    WebSettingsFeature.CLIPBOARD -> stringResource(Res.string.clipboard_sync)
}

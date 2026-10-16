package com.ismartcoding.plain.ui.permissions

import com.ismartcoding.plain.platform.system.IosPlatformRegistry
import com.ismartcoding.plain.app.events.PermissionsResultEvent
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.lib.logcat.LogCat
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Duration.Companion.seconds
import com.ismartcoding.plain.platform.permissions.*

actual fun checkNotificationPermission(stringResource: StringResource, onGranted: () -> Unit) {
    if (IosPlatformRegistry.isPermissionGranted(Permission.POST_NOTIFICATIONS.name)) {
        onGranted()
    } else {
        showNotificationConfirmDialog(stringResource) {
            coIO {
                ensureNotificationPermissionAsync()
                onGranted()
            }
        }
    }
}

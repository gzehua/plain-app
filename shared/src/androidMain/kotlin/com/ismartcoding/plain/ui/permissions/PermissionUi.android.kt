package com.ismartcoding.plain.ui.permissions

import com.ismartcoding.plain.platform.isTPlus
import com.ismartcoding.plain.platform.system.ShizukuHelper
import android.content.ComponentName
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.mainActivity
import com.ismartcoding.plain.appContextValue
import com.ismartcoding.plain.ui.permissions.Permissions
import com.ismartcoding.plain.platform.files.FileHelper
import com.ismartcoding.plain.lib.extensions.hasPermission
import org.jetbrains.compose.resources.StringResource
import com.ismartcoding.plain.platform.permissions.*

actual fun checkNotificationPermission(stringResource: StringResource, onGranted: () -> Unit) {
    Permissions.checkNotification(appContext, stringResource, onGranted)
}

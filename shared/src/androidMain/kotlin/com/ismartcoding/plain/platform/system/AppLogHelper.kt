package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.platform.files.ShareHelper

import com.ismartcoding.plain.i18n.*

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.ismartcoding.plain.Constants
import com.ismartcoding.plain.app.AppIntents
import com.ismartcoding.plain.ui.i18n.LocaleHelper
import java.io.File
import android.os.Build

object AppLogHelper {
    fun share(
        context: Context,
        file: File,
    ) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "*/*"
        val appVersion = getAppVersion()
        intent.putExtra(Intent.EXTRA_SUBJECT, LocaleHelper.getString(Res.string.share_logs) + " - PlainApp $appVersion")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(Constants.SUPPORT_EMAIL))
        intent.putExtra(Intent.EXTRA_TEXT, buildDeviceInfoText())
        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, AppIntents.AUTHORITY, file))
        val chooserIntent = Intent.createChooser(intent, LocaleHelper.getString(Res.string.share_logs))
        chooserIntent.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, ShareHelper.getExcludeComponentNames(context).toTypedArray())
        // appContext is the Application context, so FLAG_ACTIVITY_NEW_TASK is required
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }

    fun buildDeviceInfoText(): String {
        val appVersion = getAppVersion()
        return buildString {
            appendLine("--- Device Info ---")
            appendLine("App: PlainApp $appVersion")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Brand: ${Build.BRAND}")
            appendLine("Product: ${Build.PRODUCT}")
        }
    }
}
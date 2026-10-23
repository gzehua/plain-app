package com.ismartcoding.plain.features

import com.ismartcoding.plain.models.Package as AppPackage
import com.ismartcoding.plain.platform.system.PackageDetails
import androidx.core.content.pm.PackageInfoCompat

import com.ismartcoding.plain.models.Certificate
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import com.ismartcoding.plain.app.AppIntents
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.system.PackageType
import com.ismartcoding.plain.extensions.isSystemApp
import com.ismartcoding.plain.packageManager
import kotlin.time.Instant
import java.io.File
import javax.security.cert.X509Certificate
import android.provider.Settings
import androidx.core.content.FileProvider

object PackageHelper {
    private val appLabelCache: MutableMap<String, String> = HashMap()
    private val appTypeCache: MutableMap<String, PackageType> = HashMap()
    private val appCertsCache: MutableMap<String, List<Certificate>> = HashMap()

    fun getPackageInfoMap(ids: List<String>): Map<String, PackageInfo?> {
        val packages = packageManager.getInstalledPackages(0).associateBy { it.packageName }
        val map = mutableMapOf<String, PackageInfo?>()
        ids.forEach { id ->
            map[id] = packages[id]
        }

        return map
    }

    fun isInstalled(packageName: String): Boolean {
        return getPackageInfoMap(listOf(packageName))[packageName] != null
    }

    fun installedFacts(): List<AppPackage> = packageManager.getInstalledApplications(0).map { info ->
        val packageInfo = runCatching { packageManager.getPackageInfo(info.packageName, PackageManager.GET_SIGNING_CERTIFICATES) }
            .getOrElse { PackageInfo().apply { packageName = info.packageName } }
        getPackage(info, packageInfo)
    }

    private fun getAppType(appInfo: ApplicationInfo): PackageType {
        var appType = appTypeCache[appInfo.packageName]
        if (appType == null) {
            appType = if (appInfo.isSystemApp()) PackageType.SYSTEM else PackageType.USER
            appTypeCache[appInfo.packageName] = appType
        }

        return appType
    }

    fun getCerts(packageInfo: PackageInfo): List<Certificate> {
        val packageName = packageInfo.packageName
        // why packageName could be null?
        if (packageName.isNullOrBlank()) {
            return emptyList()
        }
        val certs = appCertsCache[packageName]?.toMutableList() ?: mutableListOf()
        if (certs.isEmpty()) {
            val signatures = signatures(packageInfo)
            for (signature in signatures) {
                val cert = X509Certificate.getInstance(signature.toByteArray())
                certs.add(
                    Certificate(
                        cert.issuerDN.name,
                        cert.subjectDN.name,
                        cert.serialNumber.toString(),
                        Instant.fromEpochMilliseconds(cert.notBefore.time),
                        Instant.fromEpochMilliseconds(cert.notAfter.time)
                    )
                )
            }
            appCertsCache[packageName] = certs
        }

        return certs
    }

    fun getPackage(packageName: String): AppPackage {
        val flags = PackageManager.GET_SIGNING_CERTIFICATES
        val packageInfo = packageManager.getPackageInfo(packageName, flags)
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        return getPackage(appInfo, packageInfo)
    }

    private fun getPackage(appInfo: ApplicationInfo, packageInfo: PackageInfo): AppPackage {
        return AppPackage(
            id = appInfo.packageName,
            name = getLabel(appInfo),
            type = getAppType(appInfo),
            version = packageInfo.versionName ?: "",
            path = appInfo.sourceDir,
            size = File(appInfo.publicSourceDir).length(),
            certs = getCerts(packageInfo),
            installedAt = Instant.fromEpochMilliseconds(packageInfo.firstInstallTime),
            updatedAt = Instant.fromEpochMilliseconds(packageInfo.lastUpdateTime),
        )
    }

    fun getPackageDetail(packageName: String): PackageDetails {
        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        return PackageDetails(
            item = getPackage(appInfo, packageInfo),
            sourceDir = appInfo.sourceDir.orEmpty(),
            dataDir = appInfo.dataDir.orEmpty(),
            targetSdkVersion = appInfo.targetSdkVersion,
            minSdkVersion = appInfo.minSdkVersion,
            versionCode = PackageInfoCompat.getLongVersionCode(packageInfo),
            hasLargeHeap = appInfo.flags and ApplicationInfo.FLAG_LARGE_HEAP != 0,
        )
    }

    private fun getLabel(packageInfo: ApplicationInfo): String {
        val key = packageInfo.packageName
        if (!appLabelCache.containsKey(key)) {
            try {
                appLabelCache[key] = packageManager.getApplicationLabel(packageInfo).toString()
            } catch (ex: Exception) {
                appLabelCache[key] = key
                LogCat.d(ex.toString())
            }
        }

        return appLabelCache[key] ?: ""
    }

    fun getLabel(packageName: String): String {
        try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            return getLabel(applicationInfo)
        } catch (ex: Exception) {
            LogCat.d(ex.toString())
        }

        return ""
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val res = if (drawable.intrinsicWidth > 128 || drawable.intrinsicHeight > 128) {
            Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
        } else if (drawable.intrinsicWidth <= 64 || drawable.intrinsicHeight <= 64) {
            Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(res)
        drawable.setBounds(0, 0, res.width, res.height)
        drawable.draw(canvas)
        return res
    }

    fun getIcon(packageName: String): Bitmap {
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val iconDrawable = packageManager.getApplicationIcon(applicationInfo)
        return drawableToBitmap(iconDrawable)
    }

    private fun signatures(packageInfo: PackageInfo): MutableSet<Signature> {
        val signatures: MutableSet<Signature> = mutableSetOf()
        val signingInfo = packageInfo.signingInfo
        if (signingInfo != null) {
            signingInfo.signingCertificateHistory?.let { signatures.addAll(it) }
            signingInfo.apkContentsSigners?.let { signatures.addAll(it) }
        }
        @Suppress("DEPRECATION")
        packageInfo.signatures?.let { signatures.addAll(it) }
        return signatures
    }

    fun uninstall(context: Context, packageName: String) {
        context.startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    fun canLaunch(packageName: String): Boolean {
        return packageManager.getLaunchIntentForPackage(packageName) != null
    }

    fun launch(context: Context, packageName: String) {
        context.startActivity(packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    fun viewInSettings(context: Context, packageName: String) {
        context.startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.parse("package:$packageName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    fun install(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                AppIntents.AUTHORITY,
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_RETURN_RESULT, true)
                putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.applicationInfo.packageName)
            }

            context.startActivity(intent)
            LogCat.d("APK installation intent started for ${file.name}")
        } catch (e: Exception) {
            LogCat.e("Failed to install APK: ${e.message}", e)
            throw e
        }
    }

}

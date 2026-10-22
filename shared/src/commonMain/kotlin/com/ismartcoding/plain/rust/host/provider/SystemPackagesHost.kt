package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.system.getPackageInfoMap
import com.ismartcoding.plain.platform.system.installPackage
import com.ismartcoding.plain.platform.system.installedPackageFacts
import com.ismartcoding.plain.platform.system.packageInstallProfile
import com.ismartcoding.plain.platform.system.uninstallPackage
import com.ismartcoding.plain.rust.host.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

internal object SystemPackagesHost {
    internal suspend fun handle(method: String, params: JsonObject): HostResult = when (method) {
        "systemPackageFacts" -> hostResult(installedPackageFacts())
        "systemPackageStatuses" -> {
            val ids = JsonHelper.jsonDecodeFromElement<IdsRequest>(params).ids
            hostResult(getPackageInfoMap(ids))
        }
        "systemPackageInstallProfile" -> hostResult(packageInstallProfile())
        "systemInstallPackage" -> {
            val request = JsonHelper.jsonDecodeFromElement<InstallPackageRequest>(params)
            val result = installPackage(request.paths, request.packageName, request.session)
            hostResult(PackageInstallFacts(
                packageName = result.packageName,
                lastUpdateTime = result.lastUpdateTime?.toString(),
                isNew = result.isNew,
            ))
        }
        "systemUninstallPackages" -> {
            JsonHelper.jsonDecodeFromElement<IdsRequest>(params).ids.forEach {
                uninstallPackage(it)
            }
            hostResult(true)
        }
        else -> error("Unsupported provider operation")
    }
}

@Serializable
private data class InstallPackageRequest(val paths: List<String>, val packageName: String, val session: Boolean)

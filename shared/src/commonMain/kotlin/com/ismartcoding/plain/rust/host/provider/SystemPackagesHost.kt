package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.rust.host.*
import kotlinx.serialization.Serializable

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.system.installedPackageFacts
import kotlinx.serialization.json.*

internal object SystemPackagesHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemPackageFacts" -> JsonHelper.jsonEncodeToElement(installedPackageFacts())
        "systemPackageStatuses" -> {
            val ids = JsonHelper.jsonDecodeFromElement<IdsRequest>(params).ids
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.system.getPackageInfoMap(ids))
        }
        "systemPackageInstallProfile" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.system.packageInstallProfile())
        "systemInstallPackage" -> {
            val request = JsonHelper.jsonDecodeFromElement<InstallPackageRequest>(params)
            val result = com.ismartcoding.plain.platform.system.installPackage(request.paths, request.packageName, request.session)
            JsonHelper.jsonEncodeToElement(PackageInstallFacts(
                packageName = result.packageName,
                lastUpdateTime = result.lastUpdateTime?.toString(),
                isNew = result.isNew,
            ))
        }
        "systemUninstallPackages" -> {
            JsonHelper.jsonDecodeFromElement<IdsRequest>(params).ids.forEach {
                com.ismartcoding.plain.platform.system.uninstallPackage(it)
            }
            JsonHelper.jsonEncodeToElement(true)
        }
        else -> error("Unsupported provider operation")
    }
}

@Serializable
private data class InstallPackageRequest(val paths: List<String>, val packageName: String, val session: Boolean)

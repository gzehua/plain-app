package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.system.installedPackageFacts
import kotlinx.serialization.json.*

internal object SystemPackagesHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemPackageFacts" -> JsonHelper.jsonEncodeToElement(installedPackageFacts())
        "systemPackageStatuses" -> {
            val ids = params.getValue("ids").jsonArray.map { it.jsonPrimitive.content }
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.system.getPackageInfoMap(ids))
        }
        "systemPackageInstallProfile" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.system.packageInstallProfile())
        "systemInstallPackage" -> {
            val paths = params.getValue("paths").jsonArray.map { it.jsonPrimitive.content }
            val packageName = params.getValue("packageName").jsonPrimitive.content
            val session = params.getValue("session").jsonPrimitive.boolean
            val result = com.ismartcoding.plain.platform.system.installPackage(paths, packageName, session)
            JsonHelper.jsonEncodeToElement(PackageInstallFacts(
                packageName = result.packageName,
                lastUpdateTime = result.lastUpdateTime?.toString(),
                isNew = result.isNew,
            ))
        }
        "systemUninstallPackages" -> {
            params.getValue("ids").jsonArray.map { it.jsonPrimitive.content }.forEach {
                com.ismartcoding.plain.platform.system.uninstallPackage(it)
            }
            JsonHelper.jsonEncodeToElement(true)
        }
        else -> error("Unsupported provider operation")
    }
}

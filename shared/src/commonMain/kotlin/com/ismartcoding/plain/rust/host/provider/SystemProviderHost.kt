package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.files.FileResourceHost
import com.ismartcoding.plain.platform.media.audioIsPlayingFlow
import com.ismartcoding.plain.platform.media.audioPlayerProgressAsync
import com.ismartcoding.plain.platform.media.observeImageCatalog
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.platform.system.deleteSystemProviderFacts
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

object SystemProviderHost {
    internal fun accepts(method: String): Boolean =
        method.startsWith("system") || method == "imageDecodeBounds"

    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemPackageFacts",
        "systemPackageStatuses",
        "systemInstallPackage",
        "systemPackageInstallProfile",
        "systemUninstallPackages" -> SystemPackagesHost.handle(method, params)
        "systemNotificationFacts",
        "systemCancelNotifications",
        "systemReplyNotification" -> SystemNotificationsHost.handle(method, params)
        "systemContactFacts",
        "systemContactCount",
        "systemContactIds",
        "systemContactGroupFacts",
        "systemContactSources",
        "systemCreateContact",
        "systemUpdateContact",
        "systemCreateContactGroup",
        "systemUpdateContactGroup",
        "systemDeleteContactGroup" -> SystemContactsHost.handle(method, params)
        "systemPhoneLocaleFacts",
        "systemCallFacts",
        "systemCallCount",
        "systemCallIds",
        "systemMakeCall" -> SystemCallsHost.handle(method, params)
        "systemMmsTextFacts",
        "systemSmsCountFacts",
        "systemSmsRowsFacts",
        "systemSmsIdsFacts",
        "systemSmsConversationFacts",
        "systemSmsThreadFacts",
        "systemDivideSmsText",
        "systemSendSmsParts",
        "systemDeleteSmsRows",
        "systemMmsLatest", "systemMmsLaunch", "systemMmsCandidates",
        "systemSimFacts" -> SystemSmsHost.handle(method, params)
        "systemScreenMirrorControls",
        "systemScreenMirrorResetTouch",
        "systemScreenMirrorState",
        "systemStartScreenMirror",
        "systemStopScreenMirror",
        "systemRequestScreenMirrorAudio",
        "systemRequestScreenMirrorKeyFrame",
        "systemUpdateScreenMirrorQuality" -> SystemScreenMirrorHost.handle(method, params)
        "systemImageModelsObserve" -> {
            observeImageCatalog(JsonHelper.jsonDecodeFromElement<ObserveImageCatalogRequest>(params).enabled)
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemFileResource" -> FileResourceHost.handle(params)
        "systemFileIndexId",
        "systemFileIndexRows",
        "systemFileIndexCount",
        "systemMountFacts",
        "imageDecodeBounds" -> SystemFilesHost.handle(method, params)
        "systemMediaBucketItemFacts",
        "systemMediaRows",
        "systemMediaCount",
        "systemMediaIdsFacts" -> SystemMediaHost.handle(method, params)
        "systemAudioPlaybackState" -> JsonHelper.jsonEncodeToElement(AudioPlaybackFacts(
            isPlaying = audioIsPlayingFlow().value,
            positionMs = audioPlayerProgressAsync(),
        ))
        "systemDeleteRecords" -> {
            val request = JsonHelper.jsonDecodeFromElement<DeleteRecordsRequest>(params)
            JsonHelper.jsonEncodeToElement(deleteSystemProviderFacts(
                DataType.valueOf(request.provider), request.ids.toSet()))
        }
        "systemSetClipboard",
        "systemPermissionFacts",
        "systemOpenAccessibilitySettings",
        "systemOpenWebSettings",
        "systemNetworkType",
        "systemVpnActive",
        "systemScheduleBackgroundWake",
        "systemProperties",
        "systemBatterySample",
        "systemMemoryAvailable",
        "systemCapabilities",
        "systemClearMediaCache",
        "systemRelaunchApp" -> SystemAppHost.handle(method, params)
        else -> error("Unsupported provider operation")
    }
}

@Serializable
private data class ObserveImageCatalogRequest(val enabled: Boolean)

@Serializable
private data class DeleteRecordsRequest(val provider: String, val ids: List<String>)

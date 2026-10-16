package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

object SystemProviderHost {
    internal fun accepts(method: String): Boolean =
        method.startsWith("system") || method == "fileMetadataFacts" || method == "imageDecodeBounds"

    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemPackageFacts",
        "systemPackageStatuses",
        "systemInstallPackage",
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
        "systemPhoneMetadata",
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
        "systemScreenMirrorResetTouch" -> com.ismartcoding.plain.rust.http.WebSocketHost.handle(method, params)
        "systemScreenMirrorState",
        "systemStartScreenMirror",
        "systemStopScreenMirror",
        "systemRequestScreenMirrorAudio",
        "systemRequestScreenMirrorKeyFrame",
        "systemUpdateScreenMirrorQuality" -> SystemScreenMirrorHost.handle(method, params)
        "systemImageModelsObserve" -> SystemImageSearchHost.handle(method, params)
        "systemFileResource" -> com.ismartcoding.plain.platform.files.FileResourceHost.handle(params)
        "systemFileIndexId",
        "systemFileIndexRows",
        "systemFileIndexCount",
        "fileMetadataFacts",
        "systemMountFacts",
        "imageDecodeBounds" -> SystemFilesHost.handle(method, params)
        "systemMediaBucketItemFacts",
        "systemMediaRows",
        "systemMediaCount",
        "systemMediaIdsFacts" -> SystemMediaHost.handle(method, params)
        "systemAudioPlaybackState" -> SystemAudioHost.handle(method, params)
        "systemDeleteRecords" -> SystemRecordsHost.handle(method, params)
        "systemSetClipboard",
        "systemPermissionFacts",
        "systemOpenAccessibilitySettings",
        "systemOpenWebSettings",
        "systemNetworkType",
        "systemScheduleBackgroundWake",
        "systemIpv4Address",
        "systemProperties",
        "systemBatterySample",
        "systemMemoryAvailable",
        "systemCapabilities",
        "systemLogPath",
        "systemClearMediaCache",
        "systemRelaunchApp" -> SystemAppHost.handle(method, params)
        else -> error("Unsupported provider operation")
    }
}

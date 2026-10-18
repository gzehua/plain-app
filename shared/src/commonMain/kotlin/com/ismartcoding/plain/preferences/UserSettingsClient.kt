package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.rust.request.RustContentApi
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

internal const val USER_SETTINGS_SELECTION = """userSettings {
    httpPort httpsPort darkTheme amoledDarkTheme pdfFollowDarkTheme keepAwake
    locale service desktopAccess dlna developerMode allowAnyHost
    deviceName https webAddressBarExpanded screenMirrorQuality audioPlayMode audioPlaybackSpeed
    imageGridCellsPerRow videoGridCellsPerRow showHiddenFiles feedAutoRefresh feedAutoRefreshIntervalSec feedAutoRefreshOnlyWifi
    feedFontScale editorWrapContent editorFontSize editorStatusBar lastFilePath scanHistory
    audioPlaylist chatInputText nearbyDiscoverable aiImageSearchEnabled notificationFilter pomodoroSettings
    videoPlaylist homeFeatures quickNoteDraft launcherShortcuts recentSearches recentSaveDirs
    audioSortBy videoSortBy imageSortBy docSortBy fileSortBy packageSortBy
}"""

internal suspend fun readUserSettings(
    query: suspend (String) -> JsonObject = { RustContentApi.query(it) },
): JsonObject = query(USER_SETTINGS_SELECTION).getValue("userSettings").jsonObject

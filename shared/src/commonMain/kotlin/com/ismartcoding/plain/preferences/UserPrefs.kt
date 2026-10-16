package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.graphql.models.Video as MediaVideo
import com.ismartcoding.plain.data.FilePathData
import com.ismartcoding.plain.data.NotificationFilterData
import com.ismartcoding.plain.enums.AppFeatureType
import com.ismartcoding.plain.enums.DarkTheme
import com.ismartcoding.plain.enums.MediaPlayMode
import com.ismartcoding.plain.enums.ScreenMirrorMode
import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.graphql.models.AudioItem
import com.ismartcoding.plain.graphql.models.PomodoroSettings
import com.ismartcoding.plain.graphql.models.ScreenMirrorQuality
import com.ismartcoding.plain.graphql.models.Video
import com.ismartcoding.plain.platform.Locale

/**
 * Every user preference, one declaration per field.
 *
 * Each field is a [Pref] — a `MutableStateFlow` that persists on assignment —
 * so a setting is read with `.value`, subscribed with `collect`, and changed by
 * assigning `.value`. The key and codec stay inside the declaration; no caller
 * builds a patch or names a storage key.
 *
 * Keys are the Rust serde names. Rust owns the defaults and the validation;
 * the argument here is only what the flow shows before the first load, and
 * [Prefs.load] replaces every value with the authoritative snapshot.
 */
object UserPrefs {
    val httpPort = userPref("httpPort", 8080)
    val httpsPort = userPref("httpsPort", 8443)
    val darkTheme = userEnumPref("darkTheme", DarkTheme.UseDeviceTheme) { it.value }
    val amoledDarkTheme = userPref("amoledDarkTheme", false)
    val pdfFollowDarkTheme = userPref("pdfFollowDarkTheme", false)
    val keepAwake = userPref("keepAwake", true)
    val locale = userPref("locale", "")
    val service = userPref("service", false)
    val desktopAccess = userPref("desktopAccess", true)
    val dlna = userPref("dlna", false)
    val developerMode = userPref("developerMode", false)
    val allowAnyHost = userPref("allowAnyHost", false)
    val deviceName = userPref("deviceName", "")
    val https = userPref("https", false)
    val webAddressBarExpanded = userPref("webAddressBarExpanded", false)
    val screenMirrorQuality = userPref("screenMirrorQuality", ScreenMirrorQuality(ScreenMirrorMode.HD, 1080))
    val audioPlayMode = userEnumPref("audioPlayMode", MediaPlayMode.REPEAT)
    val audioPlaybackSpeed = userPref("audioPlaybackSpeed", 1.0f)
    val imageGridCellsPerRow = userPref("imageGridCellsPerRow", 3)
    val videoGridCellsPerRow = userPref("videoGridCellsPerRow", 3)
    val showHiddenFiles = userPref("showHiddenFiles", false)
    val feedAutoRefresh = userPref("feedAutoRefresh", true)
    val feedAutoRefreshInterval = userPref("feedAutoRefreshIntervalSec", 7200)
    val feedAutoRefreshOnlyWifi = userPref("feedAutoRefreshOnlyWifi", false)
    val feedFontScale = userPref("feedFontScale", 1)
    val editorWrapContent = userPref("editorWrapContent", true)
    val editorFontSize = userPref("editorFontSize", 14)
    val editorStatusBar = userPref("editorStatusBar", true)
    val lastFilePath = userPref("lastFilePath", FilePathData("", "", ""))
    val scanHistory = userPref("scanHistory", emptyList<String>())
    val audioPlaylist = userPref("audioPlaylist", emptyList<AudioItem>())
    val chatInputText = userPref("chatInputText", "")
    val nearbyDiscoverable = userPref("nearbyDiscoverable", true)
    val aiImageSearchEnabled = userPref("aiImageSearchEnabled", false)
    val notificationFilter = userPref("notificationFilter", NotificationFilterData("blacklist", emptySet()))
    val pomodoroSettings = userPref("pomodoroSettings", PomodoroSettings(25, 5, 15, 4, true, true, "", ""))
    val videoPlaylist = userPref("videoPlaylist", emptyList<MediaVideo>())
    val homeFeatures = userPref("homeFeatures", "IMAGES|VIDEOS|AUDIO|DOCS|FILES|NOTES|FEEDS")
    val quickNoteDraft = userPref("quickNoteDraft", "")
    val launcherShortcuts = userPref("launcherShortcuts", "NOTES|DOCS|FEEDS|POMODORO_TIMER")
    val recentSearches = userPref("recentSearches", emptyList<String>())
    val recentSaveDirs = userPref("recentSaveDirs", emptyList<String>())
    val audioSortBy = userEnumPref("audioSortBy", FileSortBy.DATE_DESC)
    val videoSortBy = userEnumPref("videoSortBy", FileSortBy.TAKEN_AT_DESC)
    val imageSortBy = userEnumPref("imageSortBy", FileSortBy.TAKEN_AT_DESC)
    val docSortBy = userEnumPref("docSortBy", FileSortBy.DATE_DESC)
    val fileSortBy = userEnumPref("fileSortBy", FileSortBy.NAME_ASC)
    val packageSortBy = userEnumPref("packageSortBy", FileSortBy.NAME_ASC)

    fun localeValue(): Locale? = parseLocale(locale.value)

    fun parseLocale(value: String): Locale? =
        value.takeIf { it.isNotEmpty() }?.split('-')?.let { Locale(it[0], it.getOrElse(1) { "" }) }

    /** The stored form of a locale: `language` or `language-country`, empty for system. */
    fun formatLocale(value: Locale?): String =
        value?.let { it.language + if (it.country.isEmpty()) "" else "-${it.country}" }.orEmpty()

    // --- Commands ---
    //
    // These stay named because they are Rust-side rules, not field writes:
    // de-duplicating a search, trimming a list to its limit, or rewriting a
    // playlist entry. Each returns after re-reading, so the caller gets the
    // committed list rather than a guess.

    suspend fun recordRecentSearch(term: String): List<String> {
        PrefStore.runAction(UserAction.RecordSearch(term))
        return recentSearches.value
    }

    suspend fun removeRecentSearch(term: String): List<String> {
        PrefStore.runAction(UserAction.RemoveSearch(term))
        return recentSearches.value
    }

    suspend fun clearRecentSearches() = PrefStore.runAction(UserAction.ClearSearches)

    suspend fun recordRecentSaveDir(directory: String): List<String> {
        PrefStore.runAction(UserAction.RecordSaveDirectory(directory))
        return recentSaveDirs.value
    }

    suspend fun toggleNotificationApp(packageName: String) =
        PrefStore.runAction(UserAction.ToggleNotificationApp(packageName))

    suspend fun setNotificationMode(mode: String) = PrefStore.runAction(UserAction.SetNotificationMode(mode))

    suspend fun clearNotificationApps() = PrefStore.runAction(UserAction.ClearNotificationApps)

    suspend fun deleteVideos(paths: Set<String>) = PrefStore.runAction(UserAction.DeleteVideos(paths.toList()))

    fun parseFeatures(value: String): List<String> = value.split('|').filter { it.isNotBlank() }

    fun formatFeatures(value: List<String>): String = value.joinToString("|")

    fun selectedLauncherShortcuts(value: String): List<AppFeatureType> =
        parseFeatures(value).mapNotNull { name -> AppFeatureType.entries.find { it.name == name } }

    fun formatLauncherShortcuts(value: List<AppFeatureType>): String =
        value.joinToString("|") { it.name }
}

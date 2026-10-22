package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.rust.native.RustNativeApi

import com.ismartcoding.plain.models.Video as MediaVideo
import com.ismartcoding.plain.features.files.FilePathData
import com.ismartcoding.plain.features.notifications.NotificationFilterData
import com.ismartcoding.plain.platform.system.AppFeatureType
import com.ismartcoding.plain.ui.theme.DarkTheme
import com.ismartcoding.plain.features.audio.MediaPlayMode
import com.ismartcoding.plain.features.screenmirror.ScreenMirrorMode
import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.models.PomodoroSettings
import com.ismartcoding.plain.models.ScreenMirrorQuality
import com.ismartcoding.plain.models.Video
import com.ismartcoding.plain.platform.system.Locale

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
 * [com.ismartcoding.plain.rust.RustRuntime.start] loads the authoritative snapshot.
 */
object Prefs {
    val httpPort = pref("httpPort", 8080)
    val httpsPort = pref("httpsPort", 8443)
    val darkTheme = enumPref("darkTheme", DarkTheme.UseDeviceTheme) { it.value }
    val amoledDarkTheme = pref("amoledDarkTheme", false)
    val pdfFollowDarkTheme = pref("pdfFollowDarkTheme", false)
    val keepAwake = pref("keepAwake", true)
    val locale = pref("locale", "")
    val backgroundServiceEnabled = pref("backgroundServiceEnabled", false)
    val desktopAccess = pref("desktopAccess", true)
    val dlna = pref("dlna", false)
    val developerMode = pref("developerMode", false)
    val allowAnyHost = pref("allowAnyHost", false)
    val deviceName = pref("deviceName", "")
    val https = pref("https", false)
    val webAddressBarExpanded = pref("webAddressBarExpanded", false)
    val screenMirrorQuality = pref("screenMirrorQuality", ScreenMirrorQuality(ScreenMirrorMode.HD, 1080))
    val audioPlayMode = enumPref("audioPlayMode", MediaPlayMode.REPEAT)
    val audioPlaybackSpeed = pref("audioPlaybackSpeed", 1.0f)
    val imageGridCellsPerRow = pref("imageGridCellsPerRow", 3)
    val videoGridCellsPerRow = pref("videoGridCellsPerRow", 3)
    val showHiddenFiles = pref("showHiddenFiles", false)
    val feedAutoRefresh = pref("feedAutoRefresh", true)
    val feedAutoRefreshInterval = pref("feedAutoRefreshIntervalSec", 7200)
    val feedAutoRefreshOnlyWifi = pref("feedAutoRefreshOnlyWifi", false)
    val feedFontScale = pref("feedFontScale", 1)
    val editorWrapContent = pref("editorWrapContent", true)
    val editorFontSize = pref("editorFontSize", 14)
    val editorStatusBar = pref("editorStatusBar", true)
    val nearbyDiscoverable = pref("nearbyDiscoverable", true)
    val aiImageSearchEnabled = pref("aiImageSearchEnabled", false)
    val notificationFilter = pref("notificationFilter", NotificationFilterData("blacklist", emptySet()))
    val pomodoroSettings = pref("pomodoroSettings", PomodoroSettings(25, 5, 15, 4, true, true, "", ""))
    val appHomeFeatures = pref("appHomeFeatures", "IMAGES|VIDEOS|AUDIO|DOCS|FILES|NOTES|FEEDS")
    val launcherShortcuts = pref("launcherShortcuts", "NOTES|DOCS|FEEDS|POMODORO_TIMER")
    val audioSortBy = enumPref("audioSortBy", FileSortBy.DATE_DESC)
    val videoSortBy = enumPref("videoSortBy", FileSortBy.TAKEN_AT_DESC)
    val imageSortBy = enumPref("imageSortBy", FileSortBy.TAKEN_AT_DESC)
    val docSortBy = enumPref("docSortBy", FileSortBy.DATE_DESC)
    val fileSortBy = enumPref("fileSortBy", FileSortBy.NAME_ASC)
    val packageSortBy = enumPref("packageSortBy", FileSortBy.NAME_ASC)

    fun localeValue(): Locale? = parseLocale(locale.value)

    fun parseLocale(value: String): Locale? =
        value.takeIf { it.isNotEmpty() }?.split('-')?.let { Locale(it[0], it.getOrElse(1) { "" }) }

    /** The stored form of a locale: `language` or `language-country`, empty for system. */
    fun formatLocale(value: Locale?): String =
        value?.let { it.language + if (it.country.isEmpty()) "" else "-${it.country}" }.orEmpty()

    suspend fun toggleNotificationApp(packageName: String) =
        com.ismartcoding.plain.rust.native.RustNativeApi.toggleNotificationApp(packageName)

    suspend fun setNotificationMode(mode: String) = RustNativeApi.setNotificationFilterMode(mode)

    suspend fun clearNotificationApps() = RustNativeApi.clearNotificationFilterApps()

    fun parseFeatures(value: String): List<String> = value.split('|').filter { it.isNotBlank() }

    fun formatFeatures(value: List<String>): String = value.joinToString("|")

    fun selectedLauncherShortcuts(value: String): List<AppFeatureType> =
        parseFeatures(value).mapNotNull { name -> AppFeatureType.entries.find { it.name == name } }

    fun formatLauncherShortcuts(value: List<AppFeatureType>): String =
        value.joinToString("|") { it.name }
}

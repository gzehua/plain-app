package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.app.HttpServerManager
import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HostEvent
import com.ismartcoding.plain.app.stopHttpServiceAsync
import com.ismartcoding.plain.features.audio.MediaPlayMode
import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.features.notifications.NotificationFilterData
import com.ismartcoding.plain.features.permissions.getGrantedWebPermissionsAsync
import com.ismartcoding.plain.features.settings.PasswordType
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.models.PomodoroSettings
import com.ismartcoding.plain.models.ScreenMirrorQuality
import com.ismartcoding.plain.platform.media.onScreenMirrorQualityChanged
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.system.AppFeatureType
import com.ismartcoding.plain.platform.system.Locale
import com.ismartcoding.plain.platform.system.PlatformLock
import com.ismartcoding.plain.platform.system.setSystemLocale
import com.ismartcoding.plain.rust.native.RustNativeApi
import com.ismartcoding.plain.ui.theme.DarkTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

object Prefs {
    private val lock = PlatformLock()
    private val updates = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val flows = mutableMapOf<String, Pref<*>>()
    private var snapshot: JsonObject? = null
    private var security: MutableStateFlow<SecuritySettings>? = null
    val state: StateFlow<SecuritySettings> get() = checkNotNull(security) { "Prefs.load() has not completed" }

    suspend fun load() = applySnapshot(RustNativeApi.prefsSnapshot())

    internal fun initialValue(key: String): JsonElement = lock.withLock {
        checkNotNull(snapshot) { "Call Prefs.load() after Rust startup" }.getValue(prefSnapshotKey(key))
    }

    internal fun <T> register(pref: Pref<T>): Pref<T> = lock.withLock {
        pref.restore(pref.decode(checkNotNull(snapshot).getValue(prefSnapshotKey(pref.key)), pref.value))
        check(flows.put(prefSnapshotKey(pref.key), pref) == null)
        pref
    }

    internal fun write(key: String, value: JsonElement) {
        runBlocking(Dispatchers.IO) { RustNativeApi.setPref(key, value) }
    }

    internal suspend fun update(values: JsonObject) = applySnapshot(values, replace = false)

    internal suspend fun applySnapshot(values: JsonObject, replace: Boolean = true) = updates.withLock {
        val next = lock.withLock { if (replace) values else JsonObject(checkNotNull(snapshot) + values) }
        val nextSecurity = JsonHelper.jsonDecodeFromElement<SecuritySettings>(next)
        val previous = lock.withLock {
            val previous = snapshot
            snapshot = next
            security?.let { it.value = nextSecurity } ?: run { security = MutableStateFlow(nextSecurity) }
            values.forEach { (key, value) -> flows[key]?.restoreElement(value) }
            previous
        }
        if (previous != null) {
            if (previous["apiPermissions"] != next["apiPermissions"]) {
                sendEvent(HostEvent(EventType.PERMISSIONS_UPDATED, JsonHelper.jsonEncode(getGrantedWebPermissionsAsync())))
            }
            applyChanges(previous, next)
        }
    }

    private fun applyChanges(previous: JsonObject, current: JsonObject) {
        fun changed(key: String) = previous[prefSnapshotKey(key)] != current[prefSnapshotKey(key)]
        if (changed(PrefKeys.DEVICE_NAME)) TempData.deviceName.value = deviceName.value
        if (changed(PrefKeys.SCREEN_MIRROR_QUALITY)) onScreenMirrorQualityChanged(screenMirrorQuality.value.mode)
        if (changed(PrefKeys.LOCALE)) setSystemLocale(localeValue())
        if (changed(PrefKeys.BACKGROUND_SERVICE_ENABLED)) {
            if (backgroundServiceEnabled.value) HttpServerManager.requestStart(fromUi = false)
            else scope.launch { stopHttpServiceAsync() }
        }
    }

    val httpPort by lazy { pref<Int>(PrefKeys.HTTP_PORT) }
    val httpsPort by lazy { pref<Int>(PrefKeys.HTTPS_PORT) }
    val darkTheme by lazy { enumPref<DarkTheme>(PrefKeys.DARK_THEME) { it.value } }
    val amoledDarkTheme by lazy { pref<Boolean>(PrefKeys.AMOLED_DARK_THEME) }
    val pdfFollowDarkTheme by lazy { pref<Boolean>(PrefKeys.PDF_FOLLOW_DARK_THEME) }
    val keepAwake by lazy { pref<Boolean>(PrefKeys.KEEP_AWAKE) }
    val locale by lazy { pref<String>(PrefKeys.LOCALE) }
    val backgroundServiceEnabled by lazy { pref<Boolean>(PrefKeys.BACKGROUND_SERVICE_ENABLED) }
    val desktopAccess by lazy { pref<Boolean>(PrefKeys.DESKTOP_ACCESS) }
    val dlna by lazy { pref<Boolean>(PrefKeys.DLNA) }
    val developerMode by lazy { pref<Boolean>(PrefKeys.DEVELOPER_MODE) }
    val allowAnyHost by lazy { pref<Boolean>(PrefKeys.ALLOW_ANY_HOST) }
    val deviceName by lazy { pref<String>(PrefKeys.DEVICE_NAME) }
    val https by lazy { pref<Boolean>(PrefKeys.HTTPS) }
    val webAddressBarExpanded by lazy { pref<Boolean>(PrefKeys.WEB_ADDRESS_BAR_EXPANDED) }
    val screenMirrorQuality by lazy { pref<ScreenMirrorQuality>(PrefKeys.SCREEN_MIRROR_QUALITY) }
    val audioPlayMode by lazy { enumPref<MediaPlayMode>(PrefKeys.AUDIO_PLAY_MODE) }
    val audioPlaybackSpeed by lazy { pref<Float>(PrefKeys.AUDIO_PLAYBACK_SPEED) }
    val imageGridCellsPerRow by lazy { pref<Int>(PrefKeys.IMAGE_GRID_CELLS_PER_ROW) }
    val videoGridCellsPerRow by lazy { pref<Int>(PrefKeys.VIDEO_GRID_CELLS_PER_ROW) }
    val showHiddenFiles by lazy { pref<Boolean>(PrefKeys.SHOW_HIDDEN_FILES) }
    val feedAutoRefresh by lazy { pref<Boolean>(PrefKeys.FEED_AUTO_REFRESH) }
    val feedAutoRefreshInterval by lazy { pref<Int>(PrefKeys.FEED_AUTO_REFRESH_INTERVAL) }
    val feedAutoRefreshOnlyWifi by lazy { pref<Boolean>(PrefKeys.FEED_AUTO_REFRESH_ONLY_WIFI) }
    val feedFontScale by lazy { pref<Int>(PrefKeys.FEED_FONT_SCALE) }
    val editorWrapContent by lazy { pref<Boolean>(PrefKeys.EDITOR_WRAP_CONTENT) }
    val editorFontSize by lazy { pref<Int>(PrefKeys.EDITOR_FONT_SIZE) }
    val editorStatusBar by lazy { pref<Boolean>(PrefKeys.EDITOR_STATUS_BAR) }
    val nearbyDiscoverable by lazy { pref<Boolean>(PrefKeys.NEARBY_DISCOVERABLE) }
    val notificationFilter by lazy { pref<NotificationFilterData>(PrefKeys.NOTIFICATION_FILTER) }
    val pomodoroSettings by lazy { pref<PomodoroSettings>(PrefKeys.POMODORO_SETTINGS) }
    val appHomeFeatures by lazy { pref<String>(PrefKeys.APP_HOME_FEATURES) }
    val launcherShortcuts by lazy { pref<String>(PrefKeys.LAUNCHER_SHORTCUTS) }
    val audioSortBy by lazy { enumPref<FileSortBy>(PrefKeys.AUDIO_SORT_BY) }
    val videoSortBy by lazy { enumPref<FileSortBy>(PrefKeys.VIDEO_SORT_BY) }
    val imageSortBy by lazy { enumPref<FileSortBy>(PrefKeys.IMAGE_SORT_BY) }
    val docSortBy by lazy { enumPref<FileSortBy>(PrefKeys.DOC_SORT_BY) }
    val fileSortBy by lazy { enumPref<FileSortBy>(PrefKeys.FILE_SORT_BY) }
    val packageSortBy by lazy { enumPref<FileSortBy>(PrefKeys.PACKAGE_SORT_BY) }

    fun localeValue(): Locale? = parseLocale(locale.value)

    fun parseLocale(value: String): Locale? =
        value.takeIf { it.isNotEmpty() }?.split('-')?.let { Locale(it[0], it.getOrElse(1) { "" }) }

    fun formatLocale(value: Locale?): String =
        value?.let { it.language + if (it.country.isEmpty()) "" else "-${it.country}" }.orEmpty()

    suspend fun toggleNotificationApp(packageName: String) =
        RustNativeApi.toggleNotificationApp(packageName)

    suspend fun setNotificationMode(mode: String) = RustNativeApi.setNotificationFilterMode(mode)

    suspend fun clearNotificationApps() = RustNativeApi.clearNotificationFilterApps()

    fun parseFeatures(value: String): List<String> = value.split('|').filter { it.isNotBlank() }

    fun formatFeatures(value: List<String>): String = value.joinToString("|")

    fun selectedLauncherShortcuts(value: String): List<AppFeatureType> =
        parseFeatures(value).mapNotNull { name -> AppFeatureType.entries.find { it.name == name } }

    fun formatLauncherShortcuts(value: List<AppFeatureType>): String =
        value.joinToString("|") { it.name }

    private suspend inline fun <reified T> set(key: String, value: T) = write(key, JsonHelper.jsonEncodeToElement(value))

    suspend fun setPassword(password: String) = set(PrefKeys.PASSWORD, password)
    suspend fun setPasswordType(value: PasswordType) = set(PrefKeys.PASSWORD_TYPE, value.value)
    suspend fun setLoginRequireApproval(enabled: Boolean) = set(PrefKeys.LOGIN_REQUIRE_APPROVAL, enabled)
    suspend fun setRotateUrlToken(enabled: Boolean) = set(PrefKeys.ROTATE_URL_TOKEN_ON_RESTART, enabled)
    suspend fun resetPassword() = RustNativeApi.resetPassword()
    suspend fun resetUrlToken() = RustNativeApi.resetUrlToken()
    suspend fun resetAdbToken() = RustNativeApi.resetAdbToken()
    suspend fun setApiPermission(permission: Permission, enabled: Boolean) = set(PrefKeys.API_PERMISSIONS, mapOf(permission.name to enabled))
    suspend fun setApiPermissions(permissions: Set<String>) = set(PrefKeys.API_PERMISSIONS, permissions)
    suspend fun completeOnboarding() = RustNativeApi.completeOnboarding()
    suspend fun setMdnsHostname(hostname: String) = set(PrefKeys.MDNS_HOSTNAME, hostname)
    suspend fun setAutoCheckUpdate(value: Boolean) = set(PrefKeys.AUTO_CHECK_UPDATE, value)
    suspend fun setSkipVersion(value: String) = set(PrefKeys.SKIP_VERSION, value)
    suspend fun verifyAdbToken(token: String): Boolean = RustNativeApi.verifyAdbToken(token)
}

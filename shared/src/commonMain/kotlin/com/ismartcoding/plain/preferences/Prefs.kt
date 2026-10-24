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
import com.ismartcoding.plain.models.UpdateInfo
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

import kotlinx.coroutines.flow.asStateFlow
import com.ismartcoding.plain.rust.native.nativeValue

object Prefs {
    private val lock = PlatformLock()
    private val updates = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val observers = mutableMapOf<String, () -> Boolean>()
    internal inline fun <reified T : Any> get(key: String): T = nativeValue(RustNativeApi.getPrefResponse(key))

    internal suspend inline fun <reified T> set(key: String, value: T): Boolean {
        val changed = RustNativeApi.setPref(key, JsonHelper.jsonEncodeToElement(value))
        update(setOf(key))
        return changed
    }

    suspend fun load() {
        RustNativeApi.preferences()
        deviceName
        screenMirrorQuality
        locale
        backgroundServiceEnabled
        apiPermissions
        update(lock.withLock { observers.keys.toSet() })
    }

    internal fun <T> observe(key: String, read: () -> T): StateFlow<T> = lock.withLock {
        check(key !in observers) { "Preference already observed: $key" }
        val state = MutableStateFlow(read())
        observers[key] = {
            val next = read()
            if (state.value == next) false else {
                state.value = next
                true
            }
        }
        state.asStateFlow()
    }

    internal fun refresh(keys: Set<String>): Set<String> {
        val selected = lock.withLock { keys.mapNotNull { key -> observers[key]?.let { key to it } } }
        val changed = mutableSetOf<String>()
        for ((key, read) in selected) if (read()) changed += key
        return changed
    }

    internal suspend fun update(keys: Set<String>) = updates.withLock {
        val changed = refresh(keys)
        if (PrefKeys.API_PERMISSIONS in changed) {
            sendEvent(HostEvent(EventType.PERMISSIONS_UPDATED, JsonHelper.jsonEncode(getGrantedWebPermissionsAsync())))
        }
        if (PrefKeys.DEVICE_NAME in changed) TempData.deviceName.value = deviceName.value
        if (PrefKeys.SCREEN_MIRROR_QUALITY in changed) onScreenMirrorQualityChanged(screenMirrorQuality.value.mode)
        if (PrefKeys.LOCALE in changed) setSystemLocale(localeValue())
        if (PrefKeys.BACKGROUND_SERVICE_ENABLED in changed) {
            if (backgroundServiceEnabled.value) HttpServerManager.requestStart(fromUi = false)
            else scope.launch { stopHttpServiceAsync() }
        }
    }

    val password by lazy { observe(PrefKeys.PASSWORD) { get<String>(PrefKeys.PASSWORD) } }
    val passwordType by lazy { observe(PrefKeys.PASSWORD_TYPE) { get<Int>(PrefKeys.PASSWORD_TYPE) } }
    val loginRequireApproval by lazy { observe(PrefKeys.LOGIN_REQUIRE_APPROVAL) { get<Boolean>(PrefKeys.LOGIN_REQUIRE_APPROVAL) } }
    val rotateUrlTokenOnRestart by lazy { observe(PrefKeys.ROTATE_URL_TOKEN_ON_RESTART) { get<Boolean>(PrefKeys.ROTATE_URL_TOKEN_ON_RESTART) } }
    val urlToken by lazy { observe(PrefKeys.URL_TOKEN) { get<String>(PrefKeys.URL_TOKEN) } }
    val apiPermissions by lazy { observe(PrefKeys.API_PERMISSIONS) { get<Set<String>>(PrefKeys.API_PERMISSIONS) } }
    val updateInfo by lazy { observe(PrefKeys.UPDATE_INFO) { get<UpdateInfo>(PrefKeys.UPDATE_INFO) } }
    val appUpdateAvailable by lazy { observe(PrefKeys.APP_UPDATE_AVAILABLE) { get<Boolean>(PrefKeys.APP_UPDATE_AVAILABLE) } }
    val autoCheckUpdate by lazy { observe(PrefKeys.AUTO_CHECK_UPDATE) { get<Boolean>(PrefKeys.AUTO_CHECK_UPDATE) } }
    val adbToken by lazy { observe(PrefKeys.ADB_TOKEN) { get<String>(PrefKeys.ADB_TOKEN) } }

    val httpPort by lazy { observe(PrefKeys.HTTP_PORT) { get<Int>(PrefKeys.HTTP_PORT) } }
    val httpsPort by lazy { observe(PrefKeys.HTTPS_PORT) { get<Int>(PrefKeys.HTTPS_PORT) } }
    val darkTheme by lazy { observe(PrefKeys.DARK_THEME) {
        val code = get<Int>(PrefKeys.DARK_THEME)
        DarkTheme.entries.single { it.value == code }
    } }
    val amoledDarkTheme by lazy { observe(PrefKeys.AMOLED_DARK_THEME) { get<Boolean>(PrefKeys.AMOLED_DARK_THEME) } }
    val pdfFollowDarkTheme by lazy { observe(PrefKeys.PDF_FOLLOW_DARK_THEME) { get<Boolean>(PrefKeys.PDF_FOLLOW_DARK_THEME) } }
    val keepAwake by lazy { observe(PrefKeys.KEEP_AWAKE) { get<Boolean>(PrefKeys.KEEP_AWAKE) } }
    val locale by lazy { observe(PrefKeys.LOCALE) { get<String>(PrefKeys.LOCALE) } }
    val backgroundServiceEnabled by lazy { observe(PrefKeys.BACKGROUND_SERVICE_ENABLED) { get<Boolean>(PrefKeys.BACKGROUND_SERVICE_ENABLED) } }
    val desktopAccess by lazy { observe(PrefKeys.DESKTOP_ACCESS) { get<Boolean>(PrefKeys.DESKTOP_ACCESS) } }
    val dlna by lazy { observe(PrefKeys.DLNA) { get<Boolean>(PrefKeys.DLNA) } }
    val developerMode by lazy { observe(PrefKeys.DEVELOPER_MODE) { get<Boolean>(PrefKeys.DEVELOPER_MODE) } }
    val allowAnyHost by lazy { observe(PrefKeys.ALLOW_ANY_HOST) { get<Boolean>(PrefKeys.ALLOW_ANY_HOST) } }
    val deviceName by lazy { observe(PrefKeys.DEVICE_NAME) { get<String>(PrefKeys.DEVICE_NAME) } }
    val https by lazy { observe(PrefKeys.HTTPS) { get<Boolean>(PrefKeys.HTTPS) } }
    val webAddressBarExpanded by lazy { observe(PrefKeys.WEB_ADDRESS_BAR_EXPANDED) { get<Boolean>(PrefKeys.WEB_ADDRESS_BAR_EXPANDED) } }
    val screenMirrorQuality by lazy { observe(PrefKeys.SCREEN_MIRROR_QUALITY) { get<ScreenMirrorQuality>(PrefKeys.SCREEN_MIRROR_QUALITY) } }
    val audioPlayMode by lazy { observe(PrefKeys.AUDIO_PLAY_MODE) { MediaPlayMode.entries[get<Int>(PrefKeys.AUDIO_PLAY_MODE)] } }
    val audioPlaybackSpeed by lazy { observe(PrefKeys.AUDIO_PLAYBACK_SPEED) { get<Float>(PrefKeys.AUDIO_PLAYBACK_SPEED) } }
    val imageGridCellsPerRow by lazy { observe(PrefKeys.IMAGE_GRID_CELLS_PER_ROW) { get<Int>(PrefKeys.IMAGE_GRID_CELLS_PER_ROW) } }
    val videoGridCellsPerRow by lazy { observe(PrefKeys.VIDEO_GRID_CELLS_PER_ROW) { get<Int>(PrefKeys.VIDEO_GRID_CELLS_PER_ROW) } }
    val showHiddenFiles by lazy { observe(PrefKeys.SHOW_HIDDEN_FILES) { get<Boolean>(PrefKeys.SHOW_HIDDEN_FILES) } }
    val feedAutoRefresh by lazy { observe(PrefKeys.FEED_AUTO_REFRESH) { get<Boolean>(PrefKeys.FEED_AUTO_REFRESH) } }
    val feedAutoRefreshInterval by lazy { observe(PrefKeys.FEED_AUTO_REFRESH_INTERVAL) { get<Int>(PrefKeys.FEED_AUTO_REFRESH_INTERVAL) } }
    val feedAutoRefreshOnlyWifi by lazy { observe(PrefKeys.FEED_AUTO_REFRESH_ONLY_WIFI) { get<Boolean>(PrefKeys.FEED_AUTO_REFRESH_ONLY_WIFI) } }
    val feedFontScale by lazy { observe(PrefKeys.FEED_FONT_SCALE) { get<Int>(PrefKeys.FEED_FONT_SCALE) } }
    val editorWrapContent by lazy { observe(PrefKeys.EDITOR_WRAP_CONTENT) { get<Boolean>(PrefKeys.EDITOR_WRAP_CONTENT) } }
    val editorFontSize by lazy { observe(PrefKeys.EDITOR_FONT_SIZE) { get<Int>(PrefKeys.EDITOR_FONT_SIZE) } }
    val editorStatusBar by lazy { observe(PrefKeys.EDITOR_STATUS_BAR) { get<Boolean>(PrefKeys.EDITOR_STATUS_BAR) } }
    val nearbyDiscoverable by lazy { observe(PrefKeys.NEARBY_DISCOVERABLE) { get<Boolean>(PrefKeys.NEARBY_DISCOVERABLE) } }
    val notificationFilter by lazy { observe(PrefKeys.NOTIFICATION_FILTER) { get<NotificationFilterData>(PrefKeys.NOTIFICATION_FILTER) } }
    val pomodoroSettings by lazy { observe(PrefKeys.POMODORO_SETTINGS) { get<PomodoroSettings>(PrefKeys.POMODORO_SETTINGS) } }
    val appHomeFeatures by lazy { observe(PrefKeys.APP_HOME_FEATURES) { get<String>(PrefKeys.APP_HOME_FEATURES) } }
    val launcherShortcuts by lazy { observe(PrefKeys.LAUNCHER_SHORTCUTS) { get<String>(PrefKeys.LAUNCHER_SHORTCUTS) } }
    val audioSortBy by lazy { observe(PrefKeys.AUDIO_SORT_BY) { FileSortBy.entries[get<Int>(PrefKeys.AUDIO_SORT_BY)] } }
    val videoSortBy by lazy { observe(PrefKeys.VIDEO_SORT_BY) { FileSortBy.entries[get<Int>(PrefKeys.VIDEO_SORT_BY)] } }
    val imageSortBy by lazy { observe(PrefKeys.IMAGE_SORT_BY) { FileSortBy.entries[get<Int>(PrefKeys.IMAGE_SORT_BY)] } }
    val docSortBy by lazy { observe(PrefKeys.DOC_SORT_BY) { FileSortBy.entries[get<Int>(PrefKeys.DOC_SORT_BY)] } }
    val fileSortBy by lazy { observe(PrefKeys.FILE_SORT_BY) { FileSortBy.entries[get<Int>(PrefKeys.FILE_SORT_BY)] } }
    val packageSortBy by lazy { observe(PrefKeys.PACKAGE_SORT_BY) { FileSortBy.entries[get<Int>(PrefKeys.PACKAGE_SORT_BY)] } }

    fun localeValue(): Locale? = parseLocale(get<String>(PrefKeys.LOCALE))

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

package com.ismartcoding.plain.ui.settings

import kotlinx.coroutines.flow.collect

import com.ismartcoding.plain.preferences.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import androidx.lifecycle.ViewModel
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.notifications.DNotificationApp
import com.ismartcoding.plain.platform.notifications.getAllNotificationApps
import com.ismartcoding.plain.platform.notifications.getNotificationApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationSettingsViewModel : ViewModel() {
    private val _selectedAppsFlow = MutableStateFlow<List<DNotificationApp>>(emptyList())
    val selectedAppsFlow: StateFlow<List<DNotificationApp>> = _selectedAppsFlow

    private val _allAppsFlow = MutableStateFlow<List<DNotificationApp>>(emptyList())
    val allAppsFlow: StateFlow<List<DNotificationApp>> = _allAppsFlow

    var filterData = mutableStateOf(UserPrefs.notificationFilter.value)
    var isLoading = mutableStateOf(true)
    var showAppSelector = mutableStateOf(false)
    var appsLoaded = mutableStateOf(false)
    var searchQuery = mutableStateOf("")

    // For multiple selection in app selector
    val selectedAppIds = mutableStateListOf<String>()

    init {
        viewModelScope.launch {
            UserPrefs.notificationFilter.collect { filter ->
                filterData.value = filter
                try {
                    val apps = withIO {
                        filter.apps.mapNotNull { packageName ->
                            val app = getNotificationApp(packageName)
                            if (app == null) UserPrefs.toggleNotificationApp(packageName)
                            app
                        }
                    }
                    _selectedAppsFlow.value = apps.sortedBy { it.name }
                    appsLoaded.value = false
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    LogCat.e("Notification filter apps", error)
                } finally {
                    isLoading.value = false
                }
            }
        }
    }

    suspend fun loadAllAppsAsync() = withIO {
        if (appsLoaded.value) return@withIO

        try {
            val apps = getAllNotificationApps()
                .filter { !filterData.value.apps.contains(it.id) }
            _allAppsFlow.value = apps
            appsLoaded.value = true
        } catch (e: Exception) {
            appsLoaded.value = false
        }
    }

    suspend fun toggleModeAsync() {
        val newMode = if (filterData.value.mode == "allowlist") "blacklist" else "allowlist"
        UserPrefs.setNotificationMode(newMode)
    }

    suspend fun removeAppAsync(packageName: String) {
        UserPrefs.toggleNotificationApp(packageName)
    }

    suspend fun addAppsAsync(packageNames: List<String>) {
        packageNames.forEach { packageName ->
            UserPrefs.toggleNotificationApp(packageName)
        }
    }

    suspend fun clearAllAsync() {
        UserPrefs.clearNotificationApps()
    }

    fun clearSelectedApps() {
        selectedAppIds.clear()
    }

    fun toggleAppSelection(packageName: String) {
        if (selectedAppIds.contains(packageName)) {
            selectedAppIds.remove(packageName)
        } else {
            selectedAppIds.add(packageName)
        }
    }

    fun showAppSelectorDialog() {
        showAppSelector.value = true
    }
}

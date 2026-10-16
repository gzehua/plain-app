package com.ismartcoding.plain.ui.settings

import com.ismartcoding.plain.preferences.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.features.notifications.NotificationFilterData
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

    suspend fun loadDataAsync() {
        try {
            filterData.value = UserPrefs.notificationFilter.value
            val apps = mutableListOf<DNotificationApp>()
            withIO {
                filterData.value.apps.forEach { packageName ->
                    try {
                        val app = getNotificationApp(packageName) ?: throw IllegalStateException("not installed")
                        apps.add(app)
                    } catch (e: Exception) {
                        // App might be uninstalled, remove from list
                        UserPrefs.toggleNotificationApp(packageName)
                    }
                }
            }
            _selectedAppsFlow.value = apps.sortedBy { it.name }
            isLoading.value = false
        } catch (e: Exception) {
            isLoading.value = false
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
        filterData.value = filterData.value.copy(mode = newMode)
    }

    suspend fun removeAppAsync(packageName: String) {
        UserPrefs.toggleNotificationApp(packageName)
        filterData.value = UserPrefs.notificationFilter.value
        loadSelectedApps()
    }

    suspend fun addAppsAsync(packageNames: List<String>) {
        packageNames.forEach { packageName ->
            UserPrefs.toggleNotificationApp(packageName)
        }
        filterData.value = UserPrefs.notificationFilter.value
        loadSelectedApps()
    }

    suspend fun clearAllAsync() {
        UserPrefs.clearNotificationApps()
        filterData.value = UserPrefs.notificationFilter.value
        _selectedAppsFlow.value = emptyList()
    }

    private suspend fun loadSelectedApps() = withIO {
        val apps = mutableListOf<DNotificationApp>()
        filterData.value.apps.forEach { packageName ->
            try {
                val app = getNotificationApp(packageName) ?: throw IllegalStateException("not installed")
                apps.add(app)
            } catch (e: Exception) {
                // App might be uninstalled, remove from list
                UserPrefs.toggleNotificationApp(packageName)
            }
        }
        _selectedAppsFlow.value = apps.sortedBy { it.name }
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

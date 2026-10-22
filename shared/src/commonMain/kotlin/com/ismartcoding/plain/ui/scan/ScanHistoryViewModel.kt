package com.ismartcoding.plain.ui.scan

import com.ismartcoding.plain.preferences.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismartcoding.plain.ui.state.launchSafe
import kotlinx.coroutines.flow.asStateFlow

class ScanHistoryViewModel : ViewModel() {
    val itemsFlow = UserPrefs.scanHistory.asStateFlow()

    fun delete(value: String) {
        viewModelScope.launchSafe {
            UserPrefs.scanHistory.value = UserPrefs.scanHistory.value.filterNot { it == value }
        }
    }
}

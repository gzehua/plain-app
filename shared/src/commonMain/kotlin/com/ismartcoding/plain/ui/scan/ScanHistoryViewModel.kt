package com.ismartcoding.plain.ui.scan

import com.ismartcoding.plain.rust.native.RustNativeApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismartcoding.plain.ui.state.launchSafe
import kotlinx.coroutines.flow.asStateFlow

class ScanHistoryViewModel : ViewModel() {
    private val items = kotlinx.coroutines.flow.MutableStateFlow<List<String>>(emptyList())
    val itemsFlow = items.asStateFlow()
    init { viewModelScope.launchSafe { items.value = RustNativeApi.scanHistory() } }

    fun delete(value: String) {
        viewModelScope.launchSafe {
            RustNativeApi.removeScanHistory(value)
            items.value = RustNativeApi.scanHistory()
        }
    }
}

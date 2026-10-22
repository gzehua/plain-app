package com.ismartcoding.plain.ui.images.editor

import com.ismartcoding.plain.ui.state.launchSafe

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismartcoding.plain.features.images.editor.ImageEditorProjectsClient
import com.ismartcoding.plain.features.images.editor.ImageEditorProjectSummary
import com.ismartcoding.plain.features.images.editor.toSummary
import com.ismartcoding.plain.models.ImageEditorProject
import com.ismartcoding.plain.lib.withIO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ImageEditorViewModel : ViewModel() {
    private val _itemsFlow = MutableStateFlow<List<ImageEditorProjectSummary>>(emptyList())
    val itemsFlow: StateFlow<List<ImageEditorProjectSummary>> = _itemsFlow

    var showLoading = mutableStateOf(true)
    var selectedItem = mutableStateOf<ImageEditorProjectSummary?>(null)

    private val lock = Mutex()

    suspend fun loadAsync() = withIO {
        lock.withLock {
            _itemsFlow.value = ImageEditorProjectsClient.listAsync()
            showLoading.value = false
        }
    }

    fun delete(id: String) {
        viewModelScope.launchSafe {
            lock.withLock {
                ImageEditorProjectsClient.deleteAsync(id)
                _itemsFlow.update { it.filter { item -> item.id.value != id } }
            }
        }
    }

    fun updateItem(item: ImageEditorProject) {
        _itemsFlow.update {
            val index = it.indexOfFirst { i -> i.id.value == item.id }
            if (index != -1) {
                it.toMutableList().also { list -> list[index] = item.toSummary() }
            } else {
                listOf(item.toSummary()) + it
            }
        }
    }

}

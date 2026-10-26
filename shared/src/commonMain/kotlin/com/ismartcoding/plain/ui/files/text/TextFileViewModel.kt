package com.ismartcoding.plain.ui.files.text

import com.ismartcoding.plain.models.File as FileEntry

import com.ismartcoding.plain.preferences.*

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.ui.state.launchSafe
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.files.getFileByMediaId
import com.ismartcoding.plain.platform.files.isContentUri
import com.ismartcoding.plain.ui.components.codeeditor.EditorFileIO
import com.ismartcoding.plain.ui.components.codeeditor.EditorController
import com.ismartcoding.plain.ui.components.codeeditor.EditorLoadState
import com.ismartcoding.plain.ui.components.DialogHelper
import com.ismartcoding.plain.lib.TimeHelper
import com.ismartcoding.plain.platform.files.openByteSource as filesOpenByteSource
import com.ismartcoding.plain.platform.files.writeByteChunksStreaming as filesWriteByteChunksStreaming

class TextFileViewModel : ViewModel() {
    val controller = EditorController(viewModelScope, object : EditorFileIO {
        override fun nowMillis() = TimeHelper.nowMillis()
        override fun openByteSource(path: String) = filesOpenByteSource(path)
        override suspend fun writeByteChunksStreaming(path: String, chunks: Iterator<ByteArray>) =
            filesWriteByteChunksStreaming(path, chunks)
    })
    val showMoreActions = mutableStateOf(false)
    val file = mutableStateOf<FileEntry?>(null)
    val isExternalFile = mutableStateOf(false)

    val isDataLoading: Boolean
        get() = controller.loadState.value !is EditorLoadState.Ready && controller.loadState.value !is EditorLoadState.Error

    fun loadConfigAsync() {
        viewModelScope.launchSafe {
            controller.wrapContent.value = Prefs.editorWrapContent.value
            controller.fontSizeSp.value = Prefs.editorFontSize.value
            controller.statusBarVisible.value = Prefs.editorStatusBar.value
        }
    }

    suspend fun loadFileAsync(path: String, mediaId: String, gotoEnd: Boolean) {
        try {
            if (mediaId.isNotEmpty()) {
                file.value = getFileByMediaId(mediaId)
            }
            isExternalFile.value = isContentUri(path)
            controller.open(path, gotoEnd)
        } catch (e: Exception) {
            DialogHelper.showErrorDialog(e.toString())
            LogCat.e(e.toString())
        }
    }

    fun toggleWrapContent() {
        viewModelScope.launchSafe {
            Prefs.set(PrefKeys.EDITOR_WRAP_CONTENT, !controller.wrapContent.value)
        }
        controller.toggleWrap()
    }

    fun gotoTop() = controller.gotoTop()

    fun gotoEnd() = controller.gotoEnd()

    fun enterEditMode() = controller.enterEditMode()

    fun exitEditMode(discard: Boolean) = controller.exitEditMode(discard)

    override fun onCleared() {
        controller.close()
        super.onCleared()
    }
}

package com.ismartcoding.plain.ui.files

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.ui.navigation.PBackHandler
import com.ismartcoding.plain.platform.system.appDir
import com.ismartcoding.plain.platform.files.fileExists
import com.ismartcoding.plain.platform.files.getInternalStoragePath
import com.ismartcoding.plain.features.files.FilesType
import com.ismartcoding.plain.events.PermissionsResultEvent
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.ui.components.StoragePermissionResumeEffect
import com.ismartcoding.plain.ui.components.refreshStoragePermission
import com.ismartcoding.plain.ui.media.viewer.previewer.MediaPreviewerState
import com.ismartcoding.plain.ui.audio.AudioQueueViewModel
import com.ismartcoding.plain.ui.state.exitSearchMode
import com.ismartcoding.plain.ui.state.exitSelectMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun FilesPageEffects(
    filesVM: FilesViewModel, scope: CoroutineScope,
    folderPath: String, previewerState: MediaPreviewerState,
    audioQueueVM: AudioQueueViewModel,
) {
    val reloadAfterGrant: () -> Unit = {
        scope.launch(Dispatchers.Default) { filesVM.loadAsync() }
    }
    StoragePermissionResumeEffect(filesVM.hasPermission, reloadAfterGrant)

    PBackHandler(enabled = previewerState.visible || filesVM.selectMode.value || filesVM.showSearchBar.value || filesVM.showPasteBar.value || filesVM.canNavigateBack()) {
        when {
            previewerState.visible -> scope.launch { previewerState.closeTransform() }
            filesVM.selectMode.value -> filesVM.exitSelectMode()
            filesVM.showSearchBar.value -> {
                filesVM.exitSearchMode()
                scope.launch(Dispatchers.Default) { filesVM.loadAsync() }
            }
            filesVM.showPasteBar.value -> { filesVM.cutFiles.clear(); filesVM.copyFiles.clear(); filesVM.showPasteBar.value = false }
            filesVM.canNavigateBack() -> {
                filesVM.navigateBack()
                scope.launch(Dispatchers.Default) { filesVM.loadAsync() }
            }
        }
    }

    LaunchedEffect(folderPath) {
        withContext(Dispatchers.Default) {
            if (folderPath.isNotEmpty()) {
                if (fileExists(folderPath)) {
                    val appDataPath = appDir()
                    val type = if (folderPath.startsWith(appDataPath)) FilesType.APP else FilesType.INTERNAL_STORAGE
                    val rootPath = when (type) { FilesType.APP -> appDataPath; else -> getInternalStoragePath() }
                    filesVM.initSelectedPath(rootPath, type, folderPath, folderPath)
                } else {
                    filesVM.loadLastPathAsync()
                }
            } else {
                filesVM.loadLastPathAsync()
            }
            audioQueueVM.loadAsync()
            Prefs.showHiddenFiles.collect {
                if (filesVM.hasPermission.value || filesVM.type == FilesType.APP) {
                    filesVM.loadAsync()
                }
            }
        }
    }

    LaunchedEffect(Channel.sharedFlow) {
        Channel.sharedFlow.collect { event ->
            when (event) {
                is PermissionsResultEvent -> refreshStoragePermission(filesVM.hasPermission, reloadAfterGrant)

            }
        }
    }
}
